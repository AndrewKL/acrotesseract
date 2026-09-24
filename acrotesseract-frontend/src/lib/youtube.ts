/**
 * Turns a YouTube link into an embed URL. Port of the legacy YoutubeUrlParser, which handled:
 *   https://www.youtube.com/watch?v=1PkV-A5WTPk&index=6&list=RDq-t5nBbGhPM
 *   https://youtu.be/8gO_lxThc1M?t=12
 *   https://youtu.be/1PkV-A5WTPk?list=RDq-t5nBbGhPM&t=19
 *   https://www.youtube.com/embed/1PkV-A5WTPk?start=33
 * Returns null for anything that isn't a recognizable YouTube video link.
 */
export function youtubeEmbedUrl(link: string): string | null {
  let url: URL;
  try {
    url = new URL(link);
  } catch {
    return null;
  }

  const host = url.hostname.toLowerCase().replace(/^(www|m)\./, '');
  let videoId: string | null = null;
  if (host === 'youtu.be') {
    videoId = url.pathname.split('/')[1] ?? null;
  } else if (host === 'youtube.com' || host === 'youtube-nocookie.com') {
    const [, first, second] = url.pathname.split('/');
    if (first === 'watch') videoId = url.searchParams.get('v');
    else if (first === 'embed' || first === 'shorts' || first === 'live') videoId = second ?? null;
  }
  if (!videoId || !/^[\w-]{6,}$/.test(videoId)) return null;

  const start = parseSeconds(url.searchParams.get('t') ?? url.searchParams.get('start'));
  const embed = new URL(`https://www.youtube-nocookie.com/embed/${videoId}`);
  if (start) embed.searchParams.set('start', String(start));
  return embed.toString();
}

/** "403", "403s", "1m20s" or "1h2m3s" to seconds. */
function parseSeconds(value: string | null): number | null {
  if (!value) return null;
  if (/^\d+s?$/.test(value)) return parseInt(value, 10);
  const match = /^(?:(\d+)h)?(?:(\d+)m)?(?:(\d+)s)?$/.exec(value);
  if (!match || !value) return null;
  const [, h = '0', m = '0', s = '0'] = match;
  return parseInt(h, 10) * 3600 + parseInt(m, 10) * 60 + parseInt(s, 10);
}
