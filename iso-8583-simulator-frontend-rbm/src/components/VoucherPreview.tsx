import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";

type Alignment = "left" | "center" | "right";
type FontSize = "small" | "medium" | "large";
type FontStyle = "normal" | "bold" | "inverted";

interface VoucherLine {
  segments: VoucherSegment[];
}

interface VoucherSegment {
  text: string;
  alignment: Alignment;
  fontSize: FontSize;
  fontStyle: FontStyle;
}

function parseVoucherScript(raw: string): VoucherLine[] {
  const lines: VoucherLine[] = [];
  let currentAlignment: Alignment = "left";
  let currentSize: FontSize = "medium";
  let currentStyle: FontStyle = "normal";

  // Split by [0X0A] (line break)
  const rawLines = raw.split("[0X0A]");

  for (const rawLine of rawLines) {
    if (!rawLine && lines.length > 0) {
      lines.push({ segments: [] });
      continue;
    }
    if (!rawLine) continue;

    const segments: VoucherSegment[] = [];
    // Split by control codes but keep them
    const parts = rawLine.split(/(\[0X[0-9A-Fa-f]{2}\])/g);

    let currentText = "";

    for (const part of parts) {
      const code = part.match(/^\[0X([0-9A-Fa-f]{2})\]$/);
      if (code) {
        // Flush text before changing state
        if (currentText) {
          segments.push({
            text: currentText,
            alignment: currentAlignment,
            fontSize: currentSize,
            fontStyle: currentStyle,
          });
          currentText = "";
        }

        const hex = code[1].toUpperCase();
        switch (hex) {
          case "1A": currentAlignment = "left"; break;
          case "1B": currentAlignment = "right"; break;
          case "1C": currentAlignment = "center"; break;
          case "2A": currentSize = "small"; break;
          case "2B": currentSize = "medium"; break;
          case "2C": currentSize = "large"; break;
          case "3A": currentStyle = "normal"; break;
          case "3B": currentStyle = "bold"; break;
          case "3C": currentStyle = "inverted"; break;
          // 0A handled at split level
        }
      } else if (part) {
        currentText += part;
      }
    }

    if (currentText) {
      segments.push({
        text: currentText,
        alignment: currentAlignment,
        fontSize: currentSize,
        fontStyle: currentStyle,
      });
    }

    if (segments.length > 0) {
      lines.push({ segments });
    }
  }

  return lines;
}

function getTextAlign(alignment: Alignment): string {
  switch (alignment) {
    case "left": return "text-left";
    case "center": return "text-center";
    case "right": return "text-right";
  }
}

function getFontSize(size: FontSize): string {
  switch (size) {
    case "small": return "text-[10px] leading-snug";
    case "medium": return "text-xs leading-snug";
    case "large": return "text-sm leading-snug font-bold";
  }
}

function getStyle(style: FontStyle): string {
  switch (style) {
    case "normal": return "";
    case "bold": return "font-bold";
    case "inverted": return "font-bold bg-black text-white px-1 py-0.5";
  }
}

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  value: string;
}

export function VoucherPreview({ open, onOpenChange, value }: Props) {
  const lines = parseVoucherScript(value);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Voucher Digital Preview</DialogTitle>
        </DialogHeader>
        <div className="bg-white text-black p-6 rounded border border-border min-h-[200px] space-y-0" style={{ fontFamily: "'Courier New', Courier, monospace" }}>
          {lines.map((line, i) => {
            if (line.segments.length === 0) {
              return <div key={i} className="h-3" />;
            }

            // Check if line has mixed alignments (left + right = justified row)
            const hasLeft = line.segments.some(s => s.alignment === "left");
            const hasRight = line.segments.some(s => s.alignment === "right");

            if (hasLeft && hasRight) {
              const leftSegs = line.segments.filter(s => s.alignment === "left");
              const rightSegs = line.segments.filter(s => s.alignment === "right");
              return (
                <div key={i} className="flex justify-between items-baseline">
                  <span>
                    {leftSegs.map((seg, j) => (
                      <span key={j} className={`${getFontSize(seg.fontSize)} ${getStyle(seg.fontStyle)}`}>
                        {seg.text}
                      </span>
                    ))}
                  </span>
                  <span>
                    {rightSegs.map((seg, j) => (
                      <span key={j} className={`${getFontSize(seg.fontSize)} ${getStyle(seg.fontStyle)}`}>
                        {seg.text}
                      </span>
                    ))}
                  </span>
                </div>
              );
            }

            // Single alignment
            const alignment = line.segments[0]?.alignment || "left";
            return (
              <div key={i} className={getTextAlign(alignment)}>
                {line.segments.map((seg, j) => (
                  <span key={j} className={`${getFontSize(seg.fontSize)} ${getStyle(seg.fontStyle)}`}>
                    {seg.text}
                  </span>
                ))}
              </div>
            );
          })}
        </div>
      </DialogContent>
    </Dialog>
  );
}
