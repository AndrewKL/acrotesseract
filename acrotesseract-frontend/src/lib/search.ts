/** Case-insensitive substring match on name. Names that start with the query come first; order is otherwise kept. */
export function filterByName<T extends { name: string }>(items: readonly T[], query: string): T[] {
  const q = query.trim().toLowerCase();
  if (!q) return [...items];
  const prefix: T[] = [];
  const rest: T[] = [];
  for (const item of items) {
    const name = item.name.toLowerCase();
    if (name.startsWith(q)) prefix.push(item);
    else if (name.includes(q)) rest.push(item);
  }
  return [...prefix, ...rest];
}
