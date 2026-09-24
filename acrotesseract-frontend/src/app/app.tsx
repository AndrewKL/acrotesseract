import { useEffect, useState } from 'react';
import styles from './app.module.css';
import { fetchHealth, type Health } from './api';

export function App() {
  const [health, setHealth] = useState<Health | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchHealth()
      .then(setHealth)
      .catch((e: Error) => setError(e.message));
  }, []);

  return (
    <main className={styles.app}>
      <h1>Acro Tesseract</h1>
      <p>Acroyoga poses and the transitions between them.</p>
      <p className={styles.status}>
        {health
          ? `API: ${health.status} (${health.stage})`
          : error
            ? `API unavailable: ${error}`
            : 'Checking API…'}
      </p>
    </main>
  );
}

export default App;
