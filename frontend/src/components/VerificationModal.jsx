import React from 'react';
import { X, ShieldCheck, ShieldAlert, CheckCircle2, XCircle, Clock, User } from 'lucide-react';

export const VerificationModal = ({ isOpen, onClose, result }) => {
  if (!isOpen || !result) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4">
      <div className="bg-[#101726] border border-slate-800 rounded-2xl max-w-lg w-full p-6 shadow-2xl relative">
        <button
          onClick={onClose}
          className="absolute top-5 right-5 text-slate-400 hover:text-slate-200"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="flex items-center gap-3 mb-6">
          <div className={`p-2.5 rounded-xl border ${result.match ? 'bg-emerald-500/10 border-emerald-500/20 text-emerald-400' : 'bg-rose-500/10 border-rose-500/20 text-rose-400'}`}>
            {result.match ? <ShieldCheck className="w-6 h-6" /> : <ShieldAlert className="w-6 h-6" />}
          </div>
          <div>
            <h2 className="text-lg font-bold text-white">Cryptographic Integrity Report</h2>
            <p className="text-xs text-slate-400">Byte-level SHA-256 comparison against original baseline</p>
          </div>
        </div>

        <div className={`p-4 rounded-xl border mb-6 flex items-center gap-3 ${result.match ? 'bg-emerald-500/10 border-emerald-500/20 text-emerald-400' : 'bg-rose-500/10 border-rose-500/20 text-rose-400'}`}>
          {result.match ? <CheckCircle2 className="w-6 h-6 shrink-0" /> : <XCircle className="w-6 h-6 shrink-0" />}
          <div>
            <div className="text-sm font-bold">
              {result.match ? 'FILE INTEGRITY VERIFIED • MATCH 100%' : 'INTEGRITY CHECK FAILED • FILE MODIFIED'}
            </div>
            <div className="text-xs opacity-90">
              {result.match
                ? 'The stored physical file bitstream is identical to the baseline registered at ingestion.'
                : 'CRITICAL ALERT: Physical bitstream differs from intake hash. File has been tampered with or corrupted.'}
            </div>
          </div>
        </div>

        <div className="space-y-4 bg-slate-900/80 border border-slate-800 p-4 rounded-xl text-xs font-mono">
          <div>
            <span className="text-slate-400 block mb-1">Baseline Registered SHA-256 (Intake):</span>
            <div className="bg-slate-950 p-2.5 rounded-lg border border-slate-800 text-slate-300 break-all select-all">
              {result.originalHash}
            </div>
          </div>

          <div>
            <span className="text-slate-400 block mb-1">Freshly Computed SHA-256 (Vault Re-read):</span>
            <div className={`p-2.5 rounded-lg border break-all select-all ${result.match ? 'bg-slate-950 border-emerald-500/40 text-emerald-400' : 'bg-rose-950/40 border-rose-500 text-rose-400'}`}>
              {result.calculatedHash}
            </div>
          </div>

          <div className="pt-2 border-t border-slate-800 flex items-center justify-between text-slate-400 text-[11px]">
            <span className="flex items-center gap-1">
              <Clock className="w-3.5 h-3.5 text-cyan-400" />
              {new Date(result.verifiedAt).toLocaleString()}
            </span>
            <span className="flex items-center gap-1">
              <User className="w-3.5 h-3.5 text-cyan-400" />
              Verified by: {result.verifiedByUsername}
            </span>
          </div>
        </div>

        <div className="mt-6 flex justify-end">
          <button
            onClick={onClose}
            className="px-5 py-2.5 rounded-xl text-sm font-semibold text-white bg-slate-800 hover:bg-slate-700 transition-all"
          >
            Acknowledge & Close
          </button>
        </div>
      </div>
    </div>
  );
};
