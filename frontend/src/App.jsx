import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Navbar } from './components/Navbar';
import { Sidebar } from './components/Sidebar';
import { LoginPage } from './pages/LoginPage';
import { DashboardPage } from './pages/DashboardPage';
import { CasesPage } from './pages/CasesPage';
import { CaseDetailPage } from './pages/CaseDetailPage';
import { EvidenceVaultPage } from './pages/EvidenceVaultPage';
import { EvidenceDetailPage } from './pages/EvidenceDetailPage';
import { CustodyLedgerPage } from './pages/CustodyLedgerPage';
import { AuditLogsPage } from './pages/AuditLogsPage';

const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[#070b14]">
        <div className="w-8 h-8 border-4 border-cyan-500/20 border-t-cyan-500 rounded-full animate-spin" />
      </div>
    );
  }

  return isAuthenticated ? children : <Navigate to="/login" replace />;
};

const Layout = ({ children }) => {
  return (
    <div className="min-h-screen bg-[#070b14] flex flex-col">
      <Navbar />
      <div className="flex-1 flex">
        <Sidebar />
        <main className="flex-1 p-8 overflow-y-auto max-w-7xl mx-auto w-full">
          {children}
        </main>
      </div>
    </div>
  );
};

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />

          <Route
            path="/"
            element={
              <ProtectedRoute>
                <Layout>
                  <DashboardPage />
                </Layout>
              </ProtectedRoute>
            }
          />

          <Route
            path="/cases"
            element={
              <ProtectedRoute>
                <Layout>
                  <CasesPage />
                </Layout>
              </ProtectedRoute>
            }
          />

          <Route
            path="/cases/:caseId"
            element={
              <ProtectedRoute>
                <Layout>
                  <CaseDetailPage />
                </Layout>
              </ProtectedRoute>
            }
          />

          <Route
            path="/evidence"
            element={
              <ProtectedRoute>
                <Layout>
                  <EvidenceVaultPage />
                </Layout>
              </ProtectedRoute>
            }
          />

          <Route
            path="/evidence/:evidenceId"
            element={
              <ProtectedRoute>
                <Layout>
                  <EvidenceDetailPage />
                </Layout>
              </ProtectedRoute>
            }
          />

          <Route
            path="/custody"
            element={
              <ProtectedRoute>
                <Layout>
                  <CustodyLedgerPage />
                </Layout>
              </ProtectedRoute>
            }
          />

          <Route
            path="/audit"
            element={
              <ProtectedRoute>
                <Layout>
                  <AuditLogsPage />
                </Layout>
              </ProtectedRoute>
            }
          />

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
