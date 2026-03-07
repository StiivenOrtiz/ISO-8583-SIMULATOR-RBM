type Props = {
  field: {
    key: string;
    value: string;
    name: string;
  };
};

export function IsoFieldRow({ field }: Props) {
  return (
    <div className="border rounded p-2 space-y-1">
      <div className="flex items-center gap-2">
        <span className="font-mono text-xs font-semibold">
          {field.key}
        </span>

        <span className="text-sm font-medium">
          {field.name}
        </span>

        {field.key.startsWith("S") && (
          <span className="text-[10px] px-1.5 py-0.5 rounded bg-muted text-muted-foreground">
            secondary
          </span>
        )}
      </div>

      <pre className="text-xs bg-muted p-2 rounded">
        {field.value}
      </pre>
    </div>
  );
}
