/**
 * ISO8583 Bitmap Parser Utility
 * Converts hex bitmap to active field indices
 */

/**
 * Convert a hex string to binary string
 */
function hexToBinary(hex?: string | null): string {
  if (!hex) return '';

  return hex
    .split('')
    .map(char => {
      const n = parseInt(char, 16);
      return isNaN(n) ? '0000' : n.toString(2).padStart(4, '0');
    })
    .join('');
}


/**
 * Parse primary bitmap (P1-P64) and optionally secondary bitmap (S65-S128)
 * @param bitmapPrimary - 16 character hex string for primary bitmap
 * @param bitmapSecondary - Optional 16 character hex string for secondary bitmap
 * @returns Array of active field names (P2, P3, ... P64, S65, ... S128)
 */
export function parseISO8583Bitmap(
  bitmapPrimary?: string | null,
  bitmapSecondary?: string | null
): string[] {
  if (!bitmapPrimary) return [];

  const activeFields: string[] = [];
  const primaryBinary = hexToBinary(bitmapPrimary.toUpperCase());

  if (primaryBinary.length < 64) return [];

  // Primary bitmap (P1–P64)
  for (let i = 0; i < 64; i++) {
    if (primaryBinary[i] === '1') {
      const fieldNumber = i + 1;
      if (fieldNumber > 1) {
        activeFields.push(`P${fieldNumber}`);
      }
    }
  }

  const hasSecondaryBitmap = primaryBinary[0] === '1';

  // Secondary bitmap (S65–S128)
  if (hasSecondaryBitmap && bitmapSecondary) {
    const secondaryBinary = hexToBinary(bitmapSecondary.toUpperCase());

    if (secondaryBinary.length >= 64) {
      for (let i = 0; i < 64; i++) {
        if (secondaryBinary[i] === '1') {
          activeFields.push(`S${65 + i}`);
        }
      }
    }
  }

  return activeFields;
}

/**
 * Check if secondary bitmap is present based on primary bitmap
 */
export function hasSecondaryBitmap(bitmapPrimary?: string | null): boolean {
  if (!bitmapPrimary) return false;

  const primaryBinary = hexToBinary(bitmapPrimary.toUpperCase());
  return primaryBinary.length >= 1 && primaryBinary[0] === '1';
}

