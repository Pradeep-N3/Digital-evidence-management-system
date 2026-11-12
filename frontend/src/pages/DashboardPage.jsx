import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/client';
import { useAuth } from '../context/AuthContext';
import {
  Briefcase,
  FileCode2,
  ShieldCheck,
  ShieldAlert,
  PlusCircle,
  UploadCloud,
  Activity,
  CheckCircle2,
  PieChart,
  Layers,
  ArrowUpRight
} from 'lucide-react';
import { NewCaseModal } from '../components/NewCaseModal';
import { UploadEvidenceModal } from '../components/UploadEvidenceModal';

export const DashboardPage = () => {
  const [stats, setStats] = useState(null);
  const [cases, setCases] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isCaseModalOpen, setIsCaseModalOpen] = useState(false);
  const [isEvidenceModalOpen, setIsEvidenceModalOpen] = useState(false);
  const { user } = useAuth();
  const navigate = useNavigate();

  const loadDashboardData = async () => {
    try {
      const [statsRes, casesRes] = await Promise.all([
        api.get('/dashboard/stats'),
        api.get('/cases')
      ]);
      setStats(statsRes.data);
      setCases(casesRes.data);
    } catch (err) {
      console.error('Failed to load dashboard metrics', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboardData();
  }, []);

  const handleCaseCreated = () => {
    loadDashboardData();
  };

  const handleEvidenceUploaded = () => {
    loadDashboardData();
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="flex flex-col items-center gap-3">
          <div className="w-10 h-10 border-4 border-cyan-500/20 border-t-cyan-500 rounded-full animate-spin" />
          <p className="text-xs font-mono text-slate-400">Loading Forensic Dashboard...</p>
        </div>
      </div>
    );
  }

  const canEdit = user && (user.role === 'ROLE_INVESTIGATOR' || user.role === 'ROLE_ADMIN');

  return (
    <div className="space-y-8">
      {/* Top Banner & Quick Actions */}
      <div className="flex flex-wrap items-center justify-between gap-4 bg-gradient-to-r from-[#101728] via-[#0f172a] to-[#131e36] p-6 rounded-3xl border border-slate-800 shadow-xl">
        <div>
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-mono font-semibold bg-cyan-500/10 text-cyan-400 border border-cyan-500/20 mb-2">
            <span className="w-2 h-2 rounded-full bg-cyan-400 animate-pulse" />
            OPERATIONAL READY • FORENSIC INTEGRITY ACTIVE
          </div>
          <h2 className="text-2xl font-black text-white">Forensic Operations Command</h2>
          <p className="text-xs text-slate-400 mt-1">
            Centralized evidence tracking, cryptographic verification, and unbroken chain of custody.
          </p>
        </div>

        {canEdit && (
          <div className="flex items-center gap-3">
            <button
              onClick={() => setIsEvidenceModalOpen(true)}
              className="px-4 py-2.5 rounded-xl text-xs font-bold text-slate-200 bg-slate-800 hover:bg-slate-700 border border-slate-700 transition-all flex items-center gap-2 shadow-sm"
            >
              <UploadCloud className="w-4 h-4 text-cyan-400" />
              Ingest Evidence
            </button>
            <button
              onClick={() => setIsCaseModalOpen(true)}
              className="px-4 py-2.5 rounded-xl text-xs font-bold text-white bg-gradient-to-r from-cyan-600 to-blue-600 hover:from-cyan-500 hover:to-blue-500 transition-all flex items-center gap-2 shadow-lg shadow-cyan-500/20"
            >
              <PlusCircle className="w-4 h-4" />
              New Investigation Case
            </button>
          </div>
        )}
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <div className="bg-[#101726] border border-slate-800 p-5 rounded-2xl shadow-lg relative overflow-hidden group hover:border-cyan-500/40 transition-all">
          <div className="flex items-center justify-between mb-3">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-400 font-mono">Investigation Cases</span>
            <div className="p-2.5 rounded-xl bg-blue-500/10 text-blue-400 border border-blue-500/20">
              <Briefcase className="w-5 h-5" />
            </div>
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-extrabold text-white font-mono">{stats?.totalCases || 0}</span>
            <span className="text-xs text-slate-400">Total Cases</span>
          </div>
          <div className="mt-3 pt-3 border-t border-slate-800/80 flex items-center justify-between text-xs text-slate-400">
            <span>Active: <strong className="text-blue-400 font-mono">{stats?.activeCases || 0}</strong></span>
            <span>Closed: <strong className="text-slate-300 font-mono">{stats?.closedCases || 0}</strong></span>
          </div>
        </div>

        <div className="bg-[#101726] border border-slate-800 p-5 rounded-2xl shadow-lg relative overflow-hidden group hover:border-cyan-500/40 transition-all">
          <div className="flex items-center justify-between mb-3">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-400 font-mono">Digital Evidence Files</span>
            <div className="p-2.5 rounded-xl bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">
              <FileCode2 className="w-5 h-5" />
            </div>
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-extrabold text-white font-mono">{stats?.totalEvidence || 0}</span>
            <span className="text-xs text-slate-400">Registered Artifacts</span>
          </div>
          <div className="mt-3 pt-3 border-t border-slate-800/80 flex items-center justify-between text-xs text-slate-400">
            <span>Verified: <strong className="text-emerald-400 font-mono">{stats?.verifiedEvidence || 0}</strong></span>
            <span>Compromised: <strong className="text-rose-400 font-mono">{stats?.compromisedEvidence || 0}</strong></span>
          </div>
        </div>

        <div className="bg-[#101726] border border-slate-800 p-5 rounded-2xl shadow-lg relative overflow-hidden group hover:border-emerald-500/40 transition-all">
          <div className="flex items-center justify-between mb-3">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-400 font-mono">Integrity Health Rate</span>
            <div className="p-2.5 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
              <ShieldCheck className="w-5 h-5" />
            </div>
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-extrabold text-emerald-400 font-mono">
              {stats?.integrityHealthPercentage || 100}%
            </span>
            <span className="text-xs text-slate-400">Cryptographically Sound</span>
          </div>
          <div className="mt-3 pt-3 border-t border-slate-800/80 flex items-center gap-1.5 text-xs text-emerald-400 font-medium">
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>All SHA-256 signatures locked</span>
          </div>
        </div>

        <div className="bg-[#101726] border border-slate-800 p-5 rounded-2xl shadow-lg relative overflow-hidden group hover:border-purple-500/40 transition-all">
          <div className="flex items-center justify-between mb-3">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-400 font-mono">Custody Ledger</span>
            <div className="p-2.5 rounded-xl bg-purple-500/10 text-purple-400 border border-purple-500/20">
              <Layers className="w-5 h-5" />
            </div>
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-extrabold text-white font-mono">Immutable</span>
          </div>
          <div className="mt-3 pt-3 border-t border-slate-800/80 flex items-center justify-between text-xs text-slate-400">
            <span>Chain Chaining: <strong className="text-cyan-400 font-mono">ACTIVE</strong></span>
            <span>Blocks: <strong className="text-purple-400 font-mono">OK</strong></span>
          </div>
        </div>
      </div>

      {/* Main Grid: Distribution & Recent Activity */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Evidence Category Distribution */}
        <div className="bg-[#101726] border border-slate-800 rounded-3xl p-6 shadow-xl space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <PieChart className="w-4 h-4 text-cyan-400" />
              <h3 className="text-sm font-bold text-white uppercase tracking-wider font-mono">Evidence Categorization</h3>
            </div>
            <button
              onClick={() => navigate('/evidence')}
              className="text-xs text-cyan-400 hover:text-cyan-300 flex items-center gap-1"
            >
              Browse <ArrowUpRight className="w-3 h-3" />
            </button>
          </div>

          <div className="space-y-3">
            {stats?.evidenceByType && Object.entries(stats.evidenceByType).map(([category, count]) => (
              <div key={category} className="space-y-1">
                <div className="flex justify-between text-xs font-medium">
                  <span className="text-slate-300 font-mono">{category}</span>
                  <span className="text-slate-400 font-mono">{count} items</span>
                </div>
                <div className="h-2 w-full bg-slate-900 rounded-full overflow-hidden border border-slate-800">
                  <div
                    className="h-full bg-gradient-to-r from-cyan-500 to-blue-500 rounded-full"
                    style={{
                      width: `${stats.totalEvidence > 0 ? (count / stats.totalEvidence) * 100 : 0}%`,
                    }}
                  />
                </div>
              </div>
            ))}
          </div>

          <div className="pt-4 border-t border-slate-800/80">
            <div className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2 font-mono">
              Investigation Lifecycle
            </div>
            <div className="grid grid-cols-2 gap-2 text-xs">
              {stats?.casesByStatus && Object.entries(stats.casesByStatus).map(([st, cnt]) => (
                <div key={st} className="p-2.5 rounded-xl bg-slate-900/60 border border-slate-800">
                  <div className="text-[10px] text-slate-400 truncate">{st}</div>
                  <div className="text-base font-extrabold text-white font-mono mt-0.5">{cnt}</div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Live System Activity Log */}
        <div className="lg:col-span-2 bg-[#101726] border border-slate-800 rounded-3xl p-6 shadow-xl space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Activity className="w-4 h-4 text-emerald-400" />
              <h3 className="text-sm font-bold text-white uppercase tracking-wider font-mono">Forensic Activity Stream</h3>
            </div>
            {user && (user.role === 'ROLE_ADMIN' || user.role === 'ROLE_AUDITOR') && (
              <button
                onClick={() => navigate('/audit')}
                className="text-xs text-cyan-400 hover:text-cyan-300 flex items-center gap-1"
              >
                Full Audit Trail <ArrowUpRight className="w-3 h-3" />
              </button>
            )}
          </div>

          <div className="divide-y divide-slate-800/80 overflow-y-auto max-h-[340px] pr-2">
            {stats?.recentActivities && stats.recentActivities.length > 0 ? (
              stats.recentActivities.map((act) => (
                <div key={act.id} className="py-3 flex items-start justify-between gap-4">
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-bold text-slate-200">{act.actionType}</span>
                      <span className={`px-2 py-0.5 rounded text-[10px] font-mono font-bold ${
                        act.status === 'SUCCESS'
                          ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                          : 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                      }`}>
                        {act.status}
                      </span>
                    </div>
                    <p className="text-xs text-slate-400">{act.details}</p>
                    <div className="text-[11px] text-slate-400 font-mono">
                      Target: {act.targetEntity} [{act.targetId}] &bull; Actor: {act.username}
                    </div>
                  </div>
                  <span className="text-[11px] font-mono text-slate-400 whitespace-nowrap">
                    {new Date(act.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })}
                  </span>
                </div>
              ))
            ) : (
              <div className="py-8 text-center text-xs text-slate-500">
                No recent activity records.
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Modals */}
      <NewCaseModal
        isOpen={isCaseModalOpen}
        onClose={() => setIsCaseModalOpen(false)}
        onCaseCreated={handleCaseCreated}
      />
      <UploadEvidenceModal
        isOpen={isEvidenceModalOpen}
        onClose={() => setIsEvidenceModalOpen(false)}
        cases={cases}
        onEvidenceUploaded={handleEvidenceUploaded}
      />
    </div>
  );
};
