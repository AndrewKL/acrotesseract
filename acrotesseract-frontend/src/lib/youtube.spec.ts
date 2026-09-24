import { describe, expect, it } from 'vitest';
import { youtubeEmbedUrl } from './youtube';

describe('youtubeEmbedUrl', () => {
  it.each([
    ['https://www.youtube.com/watch?v=1PkV-A5WTPk&index=6&list=RDq-t5nBbGhPM', 'https://www.youtube-nocookie.com/embed/1PkV-A5WTPk'],
    ['https://www.youtube.com/watch?v=8gO_lxThc1M', 'https://www.youtube-nocookie.com/embed/8gO_lxThc1M'],
    ['https://youtu.be/8gO_lxThc1M?t=12', 'https://www.youtube-nocookie.com/embed/8gO_lxThc1M?start=12'],
    ['https://youtu.be/1PkV-A5WTPk?list=RDq-t5nBbGhPM&t=19', 'https://www.youtube-nocookie.com/embed/1PkV-A5WTPk?start=19'],
    ['https://www.youtube.com/embed/1PkV-A5WTPk?start=33', 'https://www.youtube-nocookie.com/embed/1PkV-A5WTPk?start=33'],
    ['https://youtu.be/g8OhDBRwhSw?t=403', 'https://www.youtube-nocookie.com/embed/g8OhDBRwhSw?start=403'],
    ['https://m.youtube.com/watch?v=g8OhDBRwhSw&t=1m20s', 'https://www.youtube-nocookie.com/embed/g8OhDBRwhSw?start=80'],
  ])('%s', (link, expected) => {
    expect(youtubeEmbedUrl(link)).toBe(expected);
  });

  it.each(['not a url', 'https://vimeo.com/123456', 'https://www.youtube.com/watch', 'https://youtu.be/'])(
    'returns null for %s',
    (link) => {
      expect(youtubeEmbedUrl(link)).toBeNull();
    },
  );
});
