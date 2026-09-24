import { lazy, Suspense } from 'react';
import { Route, Routes } from 'react-router';
import { AppShell } from '../components/AppShell';
import { Loading } from '../components/ui';
import { HomePage } from '../pages/HomePage';
import { NotFoundPage } from '../pages/NotFoundPage';
import { PoseDetailPage } from '../pages/PoseDetailPage';
import { PoseListPage } from '../pages/PoseListPage';
import { TransitionDetailPage } from '../pages/TransitionDetailPage';
import { TransitionListPage } from '../pages/TransitionListPage';

// Cytoscape is most of the bundle; load it only when the graph is opened.
const GraphPage = lazy(() => import('../pages/GraphPage').then((m) => ({ default: m.GraphPage })));

/** The route table. Paths match the legacy Play app, with UUID ids. Wrapped in a router by main.tsx (or tests). */
export function App() {
  return (
    <Routes>
      <Route element={<AppShell />}>
        <Route index element={<HomePage />} />
        <Route path="poses" element={<PoseListPage />} />
        <Route path="poses/:poseId" element={<PoseDetailPage />} />
        <Route path="transitions" element={<TransitionListPage />} />
        <Route path="transitions/:transitionId" element={<TransitionDetailPage />} />
        <Route
          path="graph"
          element={
            <Suspense fallback={<Loading />}>
              <GraphPage />
            </Suspense>
          }
        />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  );
}

export default App;
