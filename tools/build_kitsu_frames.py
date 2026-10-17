#!/usr/bin/env python3
"""Build the Kitsu companion frame drawables from the raw fox/ art.

The raw set in fox/ is AI-generated: every frame is a full 1376x768 canvas
with the fox parked somewhere inside it, and a few frames carry leftover
background-removal smears (pure-black wisps that hug the silhouette). This
script turns that pile into clean, animation-ready drawables:

  1. Erases the dark smear artifacts. Eyes, lashes and the nose are also
     near-black, so a component is only dropped when it comes within a few
     pixels of full transparency -- real facial features always sit well
     inside the silhouette.
  2. Crops to the fox and normalizes her: feet on the bottom edge, body
     height constant, and the feet centroid on a known column (the anchor
     ratio shared with the Kotlin side so the app can centre that column
     on her ground line).
  3. Bakes a vertical lift into frames that are airborne, so the app can
     draw every frame bottom-on-the-ground and still get the arc.
  4. Writes lossless WebP into app/src/main/res/drawable-nodpi/ and prints
     the Kotlin metadata (anchor ratios) for KitsuFrames.kt.

Run from the repo root:  python3 tools/build_kitsu_frames.py
Requires: Pillow, numpy, scipy  (all present in the dev environment).
"""

from __future__ import annotations

import glob
import os
import sys
from dataclasses import dataclass

import numpy as np
from PIL import Image, ImageDraw
from scipy import ndimage

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC_DIR = os.path.join(ROOT, "fox")
OUT_DIR = os.path.join(ROOT, "app", "src", "main", "res", "drawable-nodpi")
PREVIEW_DIR = os.path.join(ROOT, "build", "kitsu_preview")

# Fox height in output pixels. The hero draws her 188dp tall, so 520px
# covers a 3x-density screen (188dp * 2.75) without shipping the full-size
# 760px source art.
BODY_PX = 520

# Sliding switch windows (ms) live in Kotlin; this script only bakes pixels.
# scale < 1 shrinks a frame (used when the pose-normalization overshoots,
# e.g. the camera pulled back for a jump); lift floats the fox above her
# ground line by that fraction of her body height.
@dataclass
class Frame:
    key: str
    hold_ms: int
    scale: float = 1.0
    lift: float = 0.0
    # Flip horizontally (used to make a leap frame face the same way as the
    # rest of its sequence).
    mirror: bool = False
    # Boxes (fractions of the normalized image) where leftover smudges are
    # painted out. Hand-tuned, one entry per affected frame.
    erase_boxes: tuple[tuple[float, float, float, float], ...] = ()


FRAME_SETS: dict[str, list[Frame]] = {
    # Idle: she stands, beams, and blinks back to her content face.
    "idle": [
        Frame("White_fox_standing_position", 1700),
        Frame("White_fox_standing_up", 1100),
    ],
    # The full 360 turn. Frames are hard cuts at speed.
    "spin": [
        Frame("White_fox_spinning", 95),
        Frame("White_fox_spinning_2", 95),
        Frame("White_fox_spinning_3", 95),
        Frame("White_fox_spinning_4", 95),
        Frame("White_fox_spinning_5", 95),
        Frame("White_fox_spinning_6", 95),
        Frame("White_fox_spinning_7", 95),
        Frame("White_fox_spinning_8", 95),
    ],
    # Pounce -> land. Both leap frames are flipped to face the same way;
    # the mid-air one is baked in above her ground line.
    "pounce": [
        Frame("White_fox_pouncing", 150),
        Frame("White_fox_pouncing_2", 130, mirror=True, lift=0.10),
        Frame(
            "White_fox_landing_on_ground",
            320,
            erase_boxes=((0.02, 0.90, 0.19, 1.0),),
        ),
    ],
    # Held while the stat card is open: the head-tilt "curious" pose.
    "curious": [
        Frame("White_fox_tilting_head", 0),
    ],
}


def source_files() -> dict[str, str]:
    """Map simplified keys -> file paths (the same key form used above)."""
    out: dict[str, str] = {}
    for path in sorted(glob.glob(os.path.join(SRC_DIR, "*.png"))):
        key = (
            os.path.basename(path)
            .replace("_20261018014721", "")
            .replace(" Background Removed.png", "")
        )
        out[key] = path
    return out


def clean_smears(rgba: np.ndarray) -> int:
    """Erase near-black components that graze the silhouette edge.

    Leftover background-removal smears are pure black and run along or
    across the transparency boundary; eyes, lashes and the nose are pure
    black too but sit well inside the fox. Returns erased pixel count.
    """
    alpha = rgba[..., 3].astype(np.int16)
    rgb = rgba[..., :3].astype(np.int16)
    dark = (alpha > 140) & (rgb.max(axis=2) < 45)
    if not dark.any():
        return 0

    labels, count = ndimage.label(dark, structure=np.ones((3, 3), dtype=int))
    if count == 0:
        return 0

    # Distance of every pixel to the nearest fully transparent pixel.
    transparent = alpha < 20
    if not transparent.any():
        return 0
    dist = ndimage.distance_transform_edt(~transparent)

    erased = 0
    edge_distance = 14.0
    for label in range(1, count + 1):
        comp = labels == label
        size = int(comp.sum())
        if size < 8:
            rgba[comp, 3] = 0
            erased += size
            continue
        if float(dist[comp].min()) < edge_distance:
            rgba[comp, 3] = 0
            erased += size
    return erased


def normalize(img: Image.Image, frame: Frame) -> tuple[Image.Image, float, float]:
    """Crop to the fox, scale her to a fixed body height, feet on the floor.

    Returns (image, anchor_ratio, body_fraction): the feet-centroid column
    as a fraction of the image width, and how much of the image height the
    fox's body occupies (padding for raised tails or baked lift shrinks it).
    """
    if frame.mirror:
        img = img.transpose(Image.FLIP_LEFT_RIGHT)
    rgba = np.asarray(img.convert("RGBA")).copy()
    clean_smears(rgba)
    arr = np.asarray(Image.fromarray(rgba))

    alpha = arr[..., 3]
    bbox = Image.fromarray(alpha).getbbox()
    if bbox is None:
        raise ValueError("frame is fully transparent")
    x0, y0, x1, y1 = bbox
    crop = Image.fromarray(arr[y0:y1, x0:x1])
    a = arr[y0:y1, x0:x1, 3].astype(np.float32)
    h, w = a.shape

    # Feet column: alpha-weighted centroid of the bottom strip.
    strip_h = max(4, int(h * 0.04))
    strip = np.where(a[h - strip_h :, :] > 180, a[h - strip_h :, :], 0.0)
    ys, xs = np.nonzero(strip)
    feet_x = float(xs.mean()) if len(xs) else w / 2.0

    # Body height: top of the silhouette within a window around the feet,
    # which ignores a raised tail or an outstretched scarf.
    lo = max(0, int(feet_x - 0.30 * w))
    hi = min(w, int(feet_x + 0.30 * w))
    rows = np.nonzero((a[:, lo:hi] > 60).any(axis=1))[0]
    body_top = int(rows.min()) if len(rows) else 0
    body_h = h - body_top

    s = (BODY_PX / body_h) * frame.scale
    out_w, out_h = max(1, int(w * s)), max(1, int(h * s))
    resized = crop.resize((out_w, out_h), Image.LANCZOS)

    if frame.erase_boxes:
        px = np.asarray(resized).copy()
        for fx0, fy0, fx1, fy1 in frame.erase_boxes:
            px[int(fy0 * out_h) : int(fy1 * out_h), int(fx0 * out_w) : int(fx1 * out_w), 3] = 0
        resized = Image.fromarray(px)

    # Bake the airborne lift as transparent padding under her.
    pad = int(round(frame.lift * BODY_PX))
    body_frac = BODY_PX / out_h
    if pad > 0:
        padded = Image.new("RGBA", (out_w, out_h + pad), (0, 0, 0, 0))
        padded.paste(resized, (0, 0))
        resized = padded

    anchor = (feet_x * s) / resized.width
    return resized, anchor, body_frac


def preview(sets: dict[str, list[tuple[Image.Image, float, int]]]) -> None:
    """Write checkerboard contact sheets plus an animated GIF per set."""
    os.makedirs(PREVIEW_DIR, exist_ok=True)
    for name, frames in sets.items():
        # Filmstrip (static) for eyeballing scale and artifacts.
        cell_w, cell_h = 340, 460
        sheet = Image.new("RGB", (cell_w * len(frames), cell_h), (246, 246, 246))
        d = ImageDraw.Draw(sheet)
        for i in range(0, sheet.width, 24):
            for j in range(0, sheet.height, 24):
                if (i // 24 + j // 24) % 2:
                    d.rectangle([i, j, i + 24, j + 24], fill=(222, 222, 222))
        for i, (im, anchor, _) in enumerate(frames):
            ds = min((cell_w - 20) / im.width, (cell_h - 60) / im.height)
            thumb = im.resize((int(im.width * ds), int(im.height * ds)), Image.LANCZOS)
            cx = i * cell_w + cell_w // 2
            gy = cell_h - 20
            sheet.paste(thumb, (int(cx - anchor * thumb.width), gy - thumb.height), thumb)
            d.line([(i * cell_w + 8, gy + 2), ((i + 1) * cell_w - 8, gy + 2)], fill=(200, 120, 80), width=2)
        sheet.save(os.path.join(PREVIEW_DIR, f"{name}_sheet.png"))

        # Animated GIF on a flat light background, two small cross-fades.
        bg = (245, 245, 245)
        canvas_w, canvas_h = 520, 520
        base_h = int(0.62 * canvas_h)
        gif: list[Image.Image] = []
        durations: list[int] = []
        for idx, (im, anchor, hold) in enumerate(frames):
            ds = base_h / BODY_PX
            thumb = im.resize((int(im.width * ds), int(im.height * ds)), Image.LANCZOS)
            canvas = Image.new("RGB", (canvas_w, canvas_h), bg)
            gy = canvas_h - 26
            canvas.paste(thumb, (int(canvas_w / 2 - anchor * thumb.width), gy - thumb.height), thumb)
            d = ImageDraw.Draw(canvas)
            d.line([(40, gy + 2), (canvas_w - 40, gy + 2)], fill=(200, 120, 80), width=3)
            gif.append(canvas)
            durations.append(max(60, hold))
        if len(gif) > 1:
            gif[0].save(
                os.path.join(PREVIEW_DIR, f"{name}.gif"),
                save_all=True,
                append_images=gif[1:],
                duration=durations,
                loop=0,
                disposal=2,
            )
    print(f"previews -> {PREVIEW_DIR}")


def main() -> int:
    files = source_files()
    if not files:
        print("no source frames found in", SRC_DIR)
        return 1
    os.makedirs(OUT_DIR, exist_ok=True)

    # Drop stale frames from previous runs so renumbered sets don't leave
    # orphaned drawables behind (kitsu_companion.png is not touched: the
    # widgets still use it).
    for stale in glob.glob(os.path.join(OUT_DIR, "kitsu_*.webp")):
        os.remove(stale)

    baked: dict[str, list[tuple[Image.Image, float, int]]] = {}
    kotlin_lines: list[str] = []
    total_bytes = 0

    for set_name, frames in FRAME_SETS.items():
        entries = []
        kotlin_lines.append(f'    KitsuFrameSet.{set_name.upper()} to listOf(')
        for i, frame in enumerate(frames, start=1):
            path = files.get(frame.key)
            if path is None:
                print(f"  !! missing source frame {frame.key}")
                return 1
            img = Image.open(path)
            out, anchor, body_frac = normalize(img, frame)
            res_name = f"kitsu_{set_name}_{i}"
            out.save(os.path.join(OUT_DIR, res_name + ".webp"), "WEBP", lossless=True, quality=100, method=6)
            size = os.path.getsize(os.path.join(OUT_DIR, res_name + ".webp"))
            total_bytes += size
            entries.append((out, anchor, frame.hold_ms))
            kotlin_lines.append(
                f"        KitsuFrame(R.drawable.{res_name}, holdMs = {frame.hold_ms}, "
                f"bodyFrac = {body_frac:.3f}f, anchorX = {anchor:.3f}f),  // {out.width}x{out.height}, {size // 1024} KB"
            )
            print(
                f"{set_name:8s} {i}: {frame.key:28s} {out.width:4d}x{out.height:<4d} "
                f"anchor={anchor:.3f} body={body_frac:.3f} {size // 1024:4d} KB"
            )
        kotlin_lines.append("    ),")
        baked[set_name] = entries

    print(f"\ntotal drawable weight: {total_bytes / 1024 / 1024:.2f} MB")
    print("\n---- Kotlin metadata ----")
    print("\n".join(kotlin_lines))
    preview(baked)
    return 0


if __name__ == "__main__":
    sys.exit(main())
