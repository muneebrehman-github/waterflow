import { Navigate, Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from './auth/ProtectedRoute';
import { Layout } from './components/Layout';
import { LoginPage } from './pages/Login/LoginPage';
import { ShopsPage } from './pages/Shops/ShopsPage';

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route path="/shops" element={<ShopsPage />} />
        <Route path="/" element={<Navigate to="/shops" replace />} />
      </Route>
      <Route path="*" element={<Navigate to="/shops" replace />} />
    </Routes>
  );
}
