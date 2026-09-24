import { useState } from 'react';
import { Link, NavLink, Outlet, useLocation } from 'react-router';
import styles from './AppShell.module.css';

const links = [
  { to: '/poses', label: 'Poses' },
  { to: '/transitions', label: 'Transitions' },
  { to: '/graph', label: 'Graph' },
];

/** Header + content column for every page. The graph page gets the full width and height. */
export function AppShell() {
  const [menuOpen, setMenuOpen] = useState(false);
  const { pathname } = useLocation();
  const fullBleed = pathname.startsWith('/graph');

  return (
    <>
      <header className={styles.header}>
        <div className={styles.bar}>
          <Link to="/" className={styles.brand} onClick={() => setMenuOpen(false)}>
            Acro Tesseract
          </Link>
          <button
            type="button"
            className={styles.menuButton}
            aria-expanded={menuOpen}
            aria-controls="site-nav"
            onClick={() => setMenuOpen((open) => !open)}
          >
            Menu
          </button>
          <nav id="site-nav" aria-label="Main" className={`${styles.nav} ${menuOpen ? styles.navOpen : ''}`}>
            {links.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                onClick={() => setMenuOpen(false)}
                className={({ isActive }) => `${styles.navLink} ${isActive ? styles.active : ''}`}
              >
                {link.label}
              </NavLink>
            ))}
          </nav>
        </div>
      </header>
      <main className={fullBleed ? styles.mainFull : styles.main}>
        <Outlet />
      </main>
    </>
  );
}
