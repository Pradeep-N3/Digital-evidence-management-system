import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/client';
import { useAuth } from '../context/AuthContext';
import {
  FileCode2,
  ArrowLeft,
  ShieldCheck,
  ShieldAlert,
  Download,
  ArrowRightLeft,
  Copy,
  Check,
  Calendar,
  User,
  HardDrive,
  Briefcase,
  Layers,
  Sparkles
} from 'lucide-react';
import { StatusBadge } from '../components/StatusBadge';
import { CustodyTimeline } from '../components/CustodyTimeline';
import { VerificationModal } from '../components/VerificationModal';
import { TransferCustodyModal } from '../components/TransferCustodyModal';

export const EvidenceDetailPage = () => {
  const { evidenceId } = useParams();
  const [evidence, setEvidence] = useState(null);
  const [custodyHistory, setCustodyHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [verifying, setVerifying] = useState(false);
  const [verificationResult, setVerificationResult] = useState(null);
  const [isVerifyModalOpen, setIsVerifyModalOpen] = useState(false);
  const [isTransferModalOpen, setIsTransferModalOpen] = useState(false);
  const [copiedHash, setCopiedHash] = useState(false);
  const { user } = useAuth();
  const navigate = useNavigate();

  const loadEvidenceData = async () => {
    try {
      const [evdRes, custRes] = await Promise.all([
        api.get(`/evidence/${evidenceId}`),
        api.get(`/evidence/${evidenceId}/custody`)
      ]);
      setEvidence(evdRes.data);
      setCustodyHistory(custRes.data);
    } catch (err) {
      console.error('Failed to load evidence dossier', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadEvidenceData();
  }, [evidenceId]);

  const handleVerifyIntegrity = async () => {
    setVerifying(true);
    try {
      const res = await api.post(`/evidence/${evidenceId}/verify`);
      setVerificationResult(res.data);
      setIsVerifyModalOpen(true);
      loadEvidenceData();
    } catch (err) {
      alert('Integrity verification failed: ' + (err.response?.data?.message || err.message));
    } finally {
      setVerifying(false);
    }
  };

  const handleDownloadFile = async () => {
    try {
      const response = await api.get(`/evidence/${evidenceId}/download`, {
        responseType: 'blob',
      });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', evidence.fileName);
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
      loadEvidenceData(); // updates download record in custody history
    } catch (err) {
      alert('Download failed: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleCopyHash = () => {
    if (evidence?.originalSha256) {
      navigator.clipboard.writeText(evidence.originalSha256);
      setCopiedHash(true);
      setTimeout(() => setCopiedHash(false), 2000);
    }
  };

  const formatFileSize = (bytes) => {
    if (!bytes) return '0 B';
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="w-8 h-8 border-4 border-cyan-500/20 border-t-cyan-500 rounded-full animate-spin" />
      </div>
    );
  }

  if (!evidence) {
    return (
      <div className="text-center py-16 space-y-3">
        <p className="text-slate-400">Evidence dossier could not be found.</p>
        <button
          onClick={() => navigate('/evidence')}
          className="text-xs text-cyan-400 hover:underline"
        >
          Return to Evidence Vault
        </button>
      </div>
    );
  }

  const canTransfer = user && (user.role === 'ROLE_INVESTIGATOR' || user.role === 'ROLE_ADMIN');

  return (
    <div className="space-y-6">
      {/* Back button */}
      <button
        onClick={() => navigate('/evidence')}
        className="inline-flex items-center gap-2 text-xs font-semibold text-slate-400 hover:text-cyan-400 transition-colors"
      >
        <ArrowLeft className="w-4 h-4" />
        Back to Evidence Vault
      </button>

      {/* Main Evidence Dossier Card */}
      <div className="bg-[#101726] border border-slate-800 rounded-3xl p-6 shadow-xl space-y-6">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div className="space-y-2">
            <div className="flex items-center gap-3">
              <span className="font-mono text-sm font-bold text-cyan-400 bg-cyan-950/60 px-2.5 py-1 rounded-lg border border-cyan-800/80">
                {evidence.evidenceId}
              </span>
              <StatusBadge status={evidence.status} />
              <span className="px-2 py-0.5 rounded font-mono font-bold text-[10px] bg-slate-900 border border-slate-800 text-slate-300">
                {evidence.evidenceType} ARTIFACT
              </span>
            </div>
            <h1 className="text-2xl font-black text-white break-all">{evidence.fileName}</h1>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={handleDownloadFile}
              className="px-4 py-2.5 rounded-xl text-xs font-bold text-slate-200 bg-slate-800 hover:bg-slate-700 border border-slate-700 transition-all flex items-center gap-2 shadow-sm"
            >
              <Download className="w-4 h-4 text-cyan-400" />
              Download Artifact
            </button>

            {canTransfer && (
              <button
                onClick={() => setIsTransferModalOpen(true)}
                className="px-4 py-2.5 rounded-xl text-xs font-bold text-slate-200 bg-slate-800 hover:bg-slate-700 border border-slate-700 transition-all flex items-center gap-2 shadow-sm"
              >
                <ArrowRightLeft className="w-4 h-4 text-amber-400" />
                Transfer Custody
              </button>
            )}

            <button
              onClick={handleVerifyIntegrity}
              disabled={verifying}
              className="px-5 py-2.5 rounded-xl text-xs font-bold text-white bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 transition-all flex items-center gap-2 shadow-lg shadow-emerald-500/20 disabled:opacity-50"
            >
              <ShieldCheck className="w-4 h-4" />
              {verifying ? 'Calculating SHA-256...' : 'Verify File Integrity'}
            </button>
          </div>
        </div>

        {/* Cryptographic Baseline Banner */}
        <div className="bg-slate-950/80 border border-slate-800 p-4 rounded-2xl space-y-2">
          <div className="flex items-center justify-between text-xs font-mono">
            <span className="text-slate-400 flex items-center gap-1.5 font-semibold">
              <Sparkles className="w-3.5 h-3.5 text-cyan-400" />
              Baseline Cryptographic Hash (SHA-256 Registered upon Intake):
            </span>
            <button
              onClick={handleCopyHash}
              className="text-cyan-400 hover:text-cyan-300 flex items-center gap-1 text-[11px]"
            >
              {copiedHash ? <Check className="w-3.5 h-3.5" /> : <Copy className="w-3.5 h-3.5" />}
              {copiedHash ? 'Copied' : 'Copy Hash'}
            </button>
          </div>
          <div className="font-mono text-xs text-slate-200 bg-slate-900 p-2.5 rounded-xl border border-slate-800 select-all break-all">
            {evidence.originalSha256}
          </div>
        </div>

        {/* Metadata Grid */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 pt-4 border-t border-slate-800 text-xs">
          <div>
            <span className="text-slate-400 block text-[11px] font-mono">Associated Case</span>
            <button
              onClick={() => navigate(`/cases/${evidence.caseId}`)}
              className="font-mono font-bold text-cyan-400 hover:underline flex items-center gap-1 mt-0.5"
            >
              <Briefcase className="w-3.5 h-3.5" />
              {evidence.caseId}
            </button>
          </div>

          <div>
            <span className="text-slate-400 block text-[11px] font-mono">File Size & MIME</span>
            <span className="font-semibold text-slate-200 flex items-center gap-1.5 mt-0.5">
              <HardDrive className="w-3.5 h-3.5 text-slate-400" />
              {formatFileSize(evidence.fileSizeBytes)} &bull; {evidence.mimeType}
            </span>
          </div>

          <div>
            <span className="text-slate-400 block text-[11px] font-mono">Ingested By</span>
            <span className="font-semibold text-slate-200 flex items-center gap-1.5 mt-0.5">
              <User className="w-3.5 h-3.5 text-cyan-400" />
              {evidence.uploadedByUsername}
            </span>
          </div>

          <div>
            <span className="text-slate-400 block text-[11px] font-mono">Ingestion Date</span>
            <span className="font-semibold text-slate-200 flex items-center gap-1.5 mt-0.5">
              <Calendar className="w-3.5 h-3.5 text-slate-400" />
              {new Date(evidence.uploadedAt).toLocaleString()}
            </span>
          </div>
        </div>
      </div>

      {/* Chain of Custody Timeline Section */}
      <div className="bg-[#101726] border border-slate-800 rounded-3xl p-6 shadow-xl space-y-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Layers className="w-5 h-5 text-cyan-400" />
            <h2 className="text-base font-bold text-white uppercase tracking-wider font-mono">
              Immutable Chain of Custody Ledger ({custodyHistory.length} Blocks)
            </h2>
          </div>
          <span className="text-xs text-emerald-400 font-mono font-semibold flex items-center gap-1">
            <ShieldCheck className="w-3.5 h-3.5" /> Hash-Linked &bull; Append-Only
          </span>
        </div>

        <CustodyTimeline records={custodyHistory} />
      </div>

      {/* Verification & Transfer Modals */}
      <VerificationModal
        isOpen={isVerifyModalOpen}
        onClose={() => setIsVerifyModalOpen(false)}
        result={verificationResult}
      />
      <TransferCustodyModal
        isOpen={isTransferModalOpen}
        onClose={() => setIsTransferModalOpen(false)}
        evidence={evidence}
        onCustodyTransferred={() => {
          loadEvidenceData();
        }}
      />
    </div>
  );
};
