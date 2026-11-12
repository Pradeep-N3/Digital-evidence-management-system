import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/client';
import { useAuth } from '../context/AuthContext';
import {
  Briefcase,
  ArrowLeft,
  Calendar,
  User,
  FileDown,
  UploadCloud,
  FileCode2,
  Clock,
  ArrowRight,
  ShieldCheck,
  Check
} from 'lucide-react';
import { StatusBadge } from '../components/StatusBadge';
import { UploadEvidenceModal } from '../components/UploadEvidenceModal';

export const CaseDetailPage = () => {
  const { caseId } = useParams();
  const [caseData, setCaseData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [downloadingPdf, setDownloadingPdf] = useState(false);
  const [statusUpdating, setStatusUpdating] = useState(false);
  const [isEvidenceModalOpen, setIsEvidenceModalOpen] = useState(false);
  const { user } = useAuth();
  const navigate = useNavigate();

  const loadCase = async () => {
    try {
      const res = await api.get(`/cases/${caseId}`);
      setCaseData(res.data);
    } catch (err) {
      console.error('Failed to load case profile', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCase();
  }, [caseId]);

  const handleStatusChange = async (newStatus) => {
    setStatusUpdating(true);
    try {
      const res = await api.patch(`/cases/${caseId}/status`, { status: newStatus });
      setCaseData(res.data);
    } catch (err) {
      alert('Failed to update case status: ' + (err.response?.data?.message || err.message));
    } finally {
      setStatusUpdating(false);
    }
  };

  const handleDownloadPdf = async () => {
    setDownloadingPdf(true);
    try {
      const response = await api.get(`/reports/case/${caseId}/pdf`, {
        responseType: 'blob',
      });
      const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `Forensic_Report_${caseId}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
    } catch (err) {
      alert('Failed to generate PDF report: ' + (err.response?.data?.message || err.message));
    } finally {
      setDownloadingPdf(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="w-8 h-8 border-4 border-cyan-500/20 border-t-cyan-500 rounded-full animate-spin" />
      </div>
    );
  }

  if (!caseData) {
    return (
      <div className="text-center py-16 space-y-3">
        <p className="text-slate-400">Case profile could not be found.</p>
        <button
          onClick={() => navigate('/cases')}
          className="text-xs text-cyan-400 hover:underline"
        >
          Return to Cases Registry
        </button>
      </div>
    );
  }

  const canEdit = user && (user.role === 'ROLE_INVESTIGATOR' || user.role === 'ROLE_ADMIN');

  return (
    <div className="space-y-6">
      {/* Back button */}
      <button
        onClick={() => navigate('/cases')}
        className="inline-flex items-center gap-2 text-xs font-semibold text-slate-400 hover:text-cyan-400 transition-colors"
      >
        <ArrowLeft className="w-4 h-4" />
        Back to Cases Registry
      </button>

      {/* Case Header Card */}
      <div className="bg-[#101726] border border-slate-800 rounded-3xl p-6 shadow-xl space-y-6">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div className="space-y-2">
            <div className="flex items-center gap-3">
              <span className="font-mono text-sm font-bold text-cyan-400 bg-cyan-950/60 px-2.5 py-1 rounded-lg border border-cyan-800/80">
                {caseData.caseId}
              </span>
              <StatusBadge status={caseData.status} />
              <span className={`px-2 py-0.5 rounded font-mono font-bold text-[10px] ${
                caseData.priority === 'CRITICAL' ? 'bg-rose-500/10 text-rose-400 border border-rose-500/20' :
                caseData.priority === 'HIGH' ? 'bg-amber-500/10 text-amber-400 border border-amber-500/20' :
                caseData.priority === 'MEDIUM' ? 'bg-blue-500/10 text-blue-400 border border-blue-500/20' :
                'bg-slate-500/10 text-slate-400 border border-slate-500/20'
              }`}>
                {caseData.priority} PRIORITY
              </span>
            </div>
            <h1 className="text-2xl font-black text-white">{caseData.title}</h1>
          </div>

          <div className="flex items-center gap-3">
            {canEdit && (
              <select
                disabled={statusUpdating}
                value={caseData.status}
                onChange={(e) => handleStatusChange(e.target.value)}
                className="bg-slate-900 border border-slate-700 rounded-xl px-3 py-2 text-xs font-semibold text-slate-200 focus:outline-none focus:border-cyan-500"
              >
                <option value="OPEN">Mark OPEN</option>
                <option value="UNDER_INVESTIGATION">Mark UNDER INVESTIGATION</option>
                <option value="PENDING_REVIEW">Mark PENDING REVIEW</option>
                <option value="CLOSED">Mark CLOSED</option>
              </select>
            )}

            <button
              onClick={handleDownloadPdf}
              disabled={downloadingPdf}
              className="px-4 py-2 rounded-xl text-xs font-bold text-slate-200 bg-slate-800 hover:bg-slate-700 border border-slate-700 transition-all flex items-center gap-2 shadow-sm disabled:opacity-50"
            >
              <FileDown className="w-4 h-4 text-cyan-400" />
              {downloadingPdf ? 'Generating PDF...' : 'Court-Ready PDF Dossier'}
            </button>

            {canEdit && (
              <button
                onClick={() => setIsEvidenceModalOpen(true)}
                className="px-4 py-2 rounded-xl text-xs font-bold text-white bg-gradient-to-r from-cyan-600 to-blue-600 hover:from-cyan-500 hover:to-blue-500 transition-all flex items-center gap-2 shadow-lg shadow-cyan-500/20"
              >
                <UploadCloud className="w-4 h-4" />
                Ingest Evidence
              </button>
            )}
          </div>
        </div>

        {/* Narrative */}
        <div className="bg-slate-900/60 border border-slate-800/80 p-4 rounded-2xl text-xs text-slate-300">
          <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider block mb-1 font-mono">
            Incident Narrative & Scope:
          </span>
          <p className="leading-relaxed">{caseData.description || 'No detailed incident narrative provided.'}</p>
        </div>

        {/* Metadata stats bar */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 pt-4 border-t border-slate-800 text-xs">
          <div>
            <span className="text-slate-400 block text-[11px] font-mono">Lead Investigator</span>
            <span className="font-semibold text-slate-200 flex items-center gap-1.5 mt-0.5">
              <User className="w-3.5 h-3.5 text-cyan-400" />
              {caseData.leadInvestigator?.fullName || 'Unassigned'}
            </span>
          </div>

          <div>
            <span className="text-slate-400 block text-[11px] font-mono">Incident Occurred</span>
            <span className="font-semibold text-slate-200 flex items-center gap-1.5 mt-0.5">
              <Calendar className="w-3.5 h-3.5 text-slate-400" />
              {caseData.incidentDate ? new Date(caseData.incidentDate).toLocaleString() : 'N/A'}
            </span>
          </div>

          <div>
            <span className="text-slate-400 block text-[11px] font-mono">Registered In DECMS</span>
            <span className="font-semibold text-slate-200 flex items-center gap-1.5 mt-0.5">
              <Clock className="w-3.5 h-3.5 text-slate-400" />
              {new Date(caseData.createdAt).toLocaleDateString()}
            </span>
          </div>

          <div>
            <span className="text-slate-400 block text-[11px] font-mono">Attached Evidence</span>
            <span className="font-mono font-bold text-cyan-400 mt-0.5 block">
              {caseData.evidenceCount} registered artifacts
            </span>
          </div>
        </div>
      </div>

      {/* Attached Evidence Table */}
      <div className="bg-[#101726] border border-slate-800 rounded-3xl overflow-hidden shadow-xl space-y-4 p-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <FileCode2 className="w-5 h-5 text-cyan-400" />
            <h2 className="text-base font-bold text-white uppercase tracking-wider font-mono">
              Attached Digital Evidence Vault ({caseData.evidenceCount})
            </h2>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-900/90 text-slate-400 font-mono uppercase tracking-wider border-b border-slate-800">
              <tr>
                <th className="py-3 px-4 font-semibold">Evidence ID</th>
                <th className="py-3 px-4 font-semibold">File Name</th>
                <th className="py-3 px-4 font-semibold">Category</th>
                <th className="py-3 px-4 font-semibold">Baseline SHA-256</th>
                <th className="py-3 px-4 font-semibold">Integrity Status</th>
                <th className="py-3 px-4 font-semibold">Ingestion Date</th>
                <th className="py-3 px-4 font-semibold text-right">Details</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/80">
              {caseData.evidenceList && caseData.evidenceList.length > 0 ? (
                caseData.evidenceList.map((e) => (
                  <tr
                    key={e.evidenceId}
                    onClick={() => navigate(`/evidence/${e.evidenceId}`)}
                    className="hover:bg-slate-800/40 cursor-pointer transition-colors"
                  >
                    <td className="py-3.5 px-4 font-mono font-bold text-cyan-400">
                      {e.evidenceId}
                    </td>
                    <td className="py-3.5 px-4 font-medium text-slate-200">
                      {e.fileName}
                    </td>
                    <td className="py-3.5 px-4 font-mono text-slate-400">
                      <span className="px-2 py-0.5 rounded bg-slate-900 border border-slate-800">
                        {e.evidenceType}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 font-mono text-slate-400 text-[11px]">
                      {e.originalSha256 ? `${e.originalSha256.substring(0, 16)}...` : 'N/A'}
                    </td>
                    <td className="py-3.5 px-4">
                      <StatusBadge status={e.status} />
                    </td>
                    <td className="py-3.5 px-4 font-mono text-slate-400 text-[11px]">
                      {new Date(e.uploadedAt).toLocaleDateString()}
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      <button className="p-1.5 rounded-lg bg-slate-900 hover:bg-cyan-500/10 text-slate-400 hover:text-cyan-400 border border-slate-800 transition-colors">
                        <ArrowRight className="w-4 h-4" />
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={7} className="py-12 text-center text-slate-500">
                    No evidence files uploaded to this case yet. Click 'Ingest Evidence' to register forensic artifacts.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      <UploadEvidenceModal
        isOpen={isEvidenceModalOpen}
        onClose={() => setIsEvidenceModalOpen(false)}
        preselectedCaseId={caseId}
        onEvidenceUploaded={() => {
          loadCase();
        }}
      />
    </div>
  );
};
