export type IsoField = {
  key: string;
  value: string;
  name: string;
  num: number;
};

export function sortIsoFields(
  dataElements: Record<string, string>,
  isoNames: Record<string, string>
): IsoField[] {
  return Object.entries(dataElements)
    .map(([key, value]) => {
      const num = parseInt(key.replace(/[PS]/, ""), 10);

      return {
        key,
        value,
        num,
        name: isoNames[key] ?? "Unknown field",
      };
    })
    .sort((a, b) => a.num - b.num);
}
