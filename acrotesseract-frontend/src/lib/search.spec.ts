import { describe, expect, it } from 'vitest';
import { filterByName } from './search';

const items = [{ name: 'Front Bird' }, { name: 'Reverse Bird' }, { name: 'Bird to Throne' }, { name: 'Star' }];

describe('filterByName', () => {
  it('returns everything for an empty query', () => {
    expect(filterByName(items, '  ')).toEqual(items);
  });

  it('matches case-insensitively and puts prefix matches first', () => {
    expect(filterByName(items, 'BIRD').map((i) => i.name)).toEqual(['Bird to Throne', 'Front Bird', 'Reverse Bird']);
  });

  it('returns nothing when nothing matches', () => {
    expect(filterByName(items, 'whale')).toEqual([]);
  });
});
