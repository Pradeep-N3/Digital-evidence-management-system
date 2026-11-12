import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/client';
import { useAuth } from '../context/AuthContext';
import { FileCode2, UploadCloud, Search, Filter, ShieldCheck, ArrowRight, HardDrive } from 'lucide-react';
import { StatusBadge } from '../components/StatusBadge';
import { UploadEvidenceModal } from '../components/UploadEvidenceModal';

export const EvidenceVaultPage = () => {
  const [evidenceList, setEvidenceList] = useState([]);
  const [cases, setCases] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('ALL');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const { user } = useAuth();
  const navigate = useNavigate();

  const loadData = async () => {
    try {
      const [evdRes, casesRes] = await Promise.all([
        api.get('/evidence'),
        api.get('/cases')
      ]);
      setEvidenceList(evdRes.data);
      setCases(casesRes.data);
    } catch (err) {
      console.error('Failed to load evidence library', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const filteredEvidence = evidenceList.filter((e) => {
    const matchesSearch =
      e.fileName.toLowerCase().includes(search.toLowerCase()) ||
      e.evidenceId.toLowerCase().includes(search.toLowerCase()) ||
      (e.caseId && e.caseId.toLowerCase().includes(search.toLowerCase()));
    const matchesCategory = categoryFilter === 'ALL' || e.evidenceType === categoryFilter;
    const matchesStatus = statusFilter === 'ALL' || e.status === statusFilter;
    return matchesSearch && matchesCategory && matchesStatus;
  });

  const formatFileSize = (bytes) => {
    if (!bytes) return '0 B';
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
  };

  const canUpload = user && (user.role === 'ROLE_INVESTIGATOR' || user.role === 'ROLE_ADMIN');

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-white flex items-center gap-3">
            <FileCode2 className="w-7 h-7 text-cyan-400" />
            Central Digital Evidence Vault
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Browse all registered forensic artifacts, inspect cryptographic signatures, and track chain of custody.
          </p>
        </div>

        {canUpload && (
          <button
            onClick={() => setIsModalOpen(true)}
            className="px-4 py-2.5 rounded-xl text-xs font-bold text-white bg-gradient-to-r from-cyan-600 to-blue-600 hover:from-cyan-500 hover:to-blue-500 transition-all flex items-center gap-2 shadow-lg shadow-cyan-500/20"
          >
            <UploadCloud className="w-4 h-4" />
            Ingest Evidence Artifact
          </button>
        )}
      </div>

      {/* Search & Filters */}
      <div className="flex flex-wrap items-center gap-4 bg-[#101726] border border-slate-800 p-4 rounded-2xl">
        <div className="relative flex-1 min-w-[260px]">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search by Evidence ID (EVD-...), File Name, or Case ID..."
            className="w-full bg-slate-900 border border-slate-800 rounded-xl pl-10 pr-4 py-2 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:border-cyan-500"
          />
        </div>

        <div className="flex items-center gap-2">
          <Filter className="w-4 h-4 text-slate-400" />
          <select
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
            className="bg-slate-900 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-500"
          >
            <option value="ALL">All Categories</option>
            <option value="IMAGE">IMAGE</option>
            <option value="DOCUMENT">DOCUMENT</option>
            <option value="MEDIA">MEDIA</option>
            <option value="LOG">LOG</option>
            <option value="OTHER">OTHER</option>
          </select>

          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="bg-slate-900 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-500"
          >
            <option value="ALL">All Integrity States</option>
            <option value="VERIFIED">Verified (Match)</option>
            <option value="COMPROMISED">Compromised (Mismatch)</option>
          </select>
        </div>
      </div>

      {/* Evidence Table */}
      <div className="bg-[#101726] border border-slate-800 rounded-3xl overflow-hidden shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-900/90 text-slate-400 font-mono uppercase tracking-wider border-b border-slate-800">
              <tr>
                <th className="py-3.5 px-4 font-semibold">Evidence ID</th>
                <th className="py-3.5 px-4 font-semibold">File Name</th>
                <th className="py-3.5 px-4 font-semibold">Case Binding</th>
                <th className="py-3.5 px-4 font-semibold">Category</th>
                <th className="py-3.5 px-4 font-semibold">Size</th>
                <th className="py-3.5 px-4 font-semibold">Integrity Status</th>
                <th className="py-3.5 px-4 font-semibold">Baseline SHA-256</th>
                <th className="py-3.5 px-4 font-semibold text-right">Details</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/80">
              {loading ? (
                <tr>
                  <td colSpan={8} className="py-12 text-center text-slate-500">
                    Loading evidence vault...
                  </td>
                </tr>
              ) : filteredEvidence.length > 0 ? (
                filteredEvidence.map((e) => (
                  <tr
                    key={e.evidenceId}
                    onClick={() => navigate(`/evidence/${e.evidenceId}`)}
                    className="hover:bg-slate-800/40 cursor-pointer transition-colors"
                  >
                    <td className="py-4 px-4 font-mono font-bold text-cyan-400">
                      {e.evidenceId}
                    </td>
                    <td className="py-4 px-4 max-w-[220px]">
                      <div className="font-semibold text-slate-200 truncate">{e.fileName}</div>
                      <div className="text-[10px] text-slate-400 font-mono">{e.mimeType}</div>
                    </td>
                    <td className="py-4 px-4 font-mono text-slate-300">
                      <span className="text-cyan-400 font-bold">{e.caseId}</span>
                    </td>
                    <td className="py-4 px-4 font-mono">
                      <span className="px-2 py-0.5 rounded bg-slate-900 border border-slate-800 text-slate-300">
                        {e.evidenceType}
                      </span>
                    </td>
                    <td className="py-4 px-4 font-mono text-slate-400">
                      {formatFileSize(e.fileSizeBytes)}
                    </td>
                    <td className="py-4 px-4">
                      <StatusBadge status={e.status} />
                    </td>
                    <td className="py-4 px-4 font-mono text-slate-400 text-[11px]">
                      {e.originalSha256 ? `${e.originalSha256.substring(0, 16)}...` : 'N/A'}
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
                    No evidence items match the specified filters.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      <UploadEvidenceModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        cases={cases}
        onEvidenceUploaded={() => {
          loadData();
        }}
      />
    </div>
  );
};
