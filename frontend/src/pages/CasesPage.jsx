import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/client';
import { useAuth } from '../context/AuthContext';
import { Briefcase, PlusCircle, Search, Filter, Calendar, User, ArrowRight } from 'lucide-react';
import { StatusBadge } from '../components/StatusBadge';
import { NewCaseModal } from '../components/NewCaseModal';

export const CasesPage = () => {
  const [cases, setCases] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const { user } = useAuth();
  const navigate = useNavigate();

  const loadCases = async () => {
    try {
      const res = await api.get('/cases');
      setCases(res.data);
    } catch (err) {
      console.error('Failed to fetch cases', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCases();
  }, []);

  const filteredCases = cases.filter((c) => {
    const matchesSearch =
      c.title.toLowerCase().includes(search.toLowerCase()) ||
      c.caseId.toLowerCase().includes(search.toLowerCase());
    const matchesStatus = statusFilter === 'ALL' || c.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const canCreate = user && (user.role === 'ROLE_INVESTIGATOR' || user.role === 'ROLE_ADMIN');

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-white flex items-center gap-3">
            <Briefcase className="w-7 h-7 text-cyan-400" />
            Investigation Cases Registry
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Centrally organize cybercrime investigations, associate evidence, and monitor lifecycle.
          </p>
        </div>

        {canCreate && (
          <button
            onClick={() => setIsModalOpen(true)}
            className="px-4 py-2.5 rounded-xl text-xs font-bold text-white bg-gradient-to-r from-cyan-600 to-blue-600 hover:from-cyan-500 hover:to-blue-500 transition-all flex items-center gap-2 shadow-lg shadow-cyan-500/20"
          >
            <PlusCircle className="w-4 h-4" />
            Open New Case
          </button>
        )}
      </div>

      {/* Controls: Search & Filters */}
      <div className="flex flex-wrap items-center gap-4 bg-[#101726] border border-slate-800 p-4 rounded-2xl">
        <div className="relative flex-1 min-w-[260px]">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search by Case ID (CASE-2026-...) or title..."
            className="w-full bg-slate-900 border border-slate-800 rounded-xl pl-10 pr-4 py-2 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:border-cyan-500"
          />
        </div>

        <div className="flex items-center gap-2">
          <Filter className="w-4 h-4 text-slate-400" />
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="bg-slate-900 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-500"
          >
            <option value="ALL">All Statuses</option>
            <option value="OPEN">Open</option>
            <option value="UNDER_INVESTIGATION">Under Investigation</option>
            <option value="PENDING_REVIEW">Pending Review</option>
            <option value="CLOSED">Closed</option>
          </select>
        </div>
      </div>

      {/* Case Table */}
      <div className="bg-[#101726] border border-slate-800 rounded-3xl overflow-hidden shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-900/90 text-slate-400 font-mono uppercase tracking-wider border-b border-slate-800">
              <tr>
                <th className="py-3.5 px-4 font-semibold">Case ID</th>
                <th className="py-3.5 px-4 font-semibold">Case Title / Incident</th>
                <th className="py-3.5 px-4 font-semibold">Status</th>
                <th className="py-3.5 px-4 font-semibold">Priority</th>
                <th className="py-3.5 px-4 font-semibold">Lead Investigator</th>
                <th className="py-3.5 px-4 font-semibold">Evidence</th>
                <th className="py-3.5 px-4 font-semibold">Incident Date</th>
                <th className="py-3.5 px-4 font-semibold text-right">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/80">
              {loading ? (
                <tr>
                  <td colSpan={8} className="py-12 text-center text-slate-500">
                    Loading cases...
                  </td>
                </tr>
              ) : filteredCases.length > 0 ? (
                filteredCases.map((c) => (
                  <tr
                    key={c.caseId}
                    onClick={() => navigate(`/cases/${c.caseId}`)}
                    className="hover:bg-slate-800/40 cursor-pointer transition-colors"
                  >
                    <td className="py-4 px-4 font-mono font-bold text-cyan-400">
                      {c.caseId}
                    </td>
                    <td className="py-4 px-4 max-w-[280px]">
                      <div className="font-semibold text-slate-200 truncate">{c.title}</div>
                      <div className="text-[11px] text-slate-400 truncate">{c.description || 'No description'}</div>
                    </td>
                    <td className="py-4 px-4">
                      <StatusBadge status={c.status} />
                    </td>
                    <td className="py-4 px-4">
                      <span className={`px-2 py-0.5 rounded font-mono font-bold text-[10px] ${
                        c.priority === 'CRITICAL' ? 'bg-rose-500/10 text-rose-400 border border-rose-500/20' :
                        c.priority === 'HIGH' ? 'bg-amber-500/10 text-amber-400 border border-amber-500/20' :
                        c.priority === 'MEDIUM' ? 'bg-blue-500/10 text-blue-400 border border-blue-500/20' :
                        'bg-slate-500/10 text-slate-400 border border-slate-500/20'
                      }`}>
                        {c.priority}
                      </span>
                    </td>
                    <td className="py-4 px-4">
                      <div className="flex items-center gap-1.5 text-slate-300">
                        <User className="w-3.5 h-3.5 text-slate-400" />
                        <span>{c.leadInvestigator?.fullName || 'Unassigned'}</span>
                      </div>
                    </td>
                    <td className="py-4 px-4 font-mono text-slate-300">
                      <span className="px-2 py-0.5 rounded bg-slate-900 border border-slate-800">
                        {c.evidenceCount} items
                      </span>
                    </td>
                    <td className="py-4 px-4 font-mono text-slate-400 text-[11px]">
                      {c.incidentDate ? new Date(c.incidentDate).toLocaleDateString() : 'N/A'}
                    </td>
                    <td className="py-4 px-4 text-right">
                      <button className="p-1.5 rounded-lg bg-slate-900 hover:bg-cyan-500/10 text-slate-400 hover:text-cyan-400 border border-slate-800 transition-colors">
                        <ArrowRight className="w-4 h-4" />
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={8} className="py-12 text-center text-slate-500">
                    No investigation cases match the selected criteria.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      <NewCaseModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onCaseCreated={() => {
          loadCases();
        }}
      />
    </div>
  );
};
