import React, { useState, useEffect } from 'react';
import api from '../api/client';
import { Link2, ShieldCheck, ShieldAlert, CheckCircle2, RefreshCw, Calendar, ArrowRight, User } from 'lucide-react';

export const CustodyLedgerPage = () => {
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [validating, setValidating] = useState(false);
  const [validationReport, setValidationReport] = useState(null);

  const loadCustodyRecords = async () => {
    try {
      const res = await api.get('/audit/custody-records');
      setRecords(res.data);
    } catch (err) {
      console.error('Failed to load custody records', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCustodyRecords();
  }, []);

  const handleValidateLedger = async () => {
    setValidating(true);
    try {
      const res = await api.get('/audit/validate-all');
      setValidationReport(res.data);
    } catch (err) {
      alert('Ledger validation failed: ' + (err.response?.data?.message || err.message));
    } finally {
      setValidating(false);
    }
  };

  const getActionColor = (action) => {
    switch (action) {
      case 'UPLOADED':
        return 'text-blue-400 bg-blue-500/10 border-blue-500/30';
      case 'VERIFIED':
        return 'text-emerald-400 bg-emerald-500/10 border-emerald-500/30';
      case 'TRANSFERRED':
        return 'text-amber-400 bg-amber-500/10 border-amber-500/30';
      case 'DOWNLOADED':
        return 'text-purple-400 bg-purple-500/10 border-purple-500/30';
      default:
        return 'text-cyan-400 bg-cyan-500/10 border-cyan-500/30';
    }
  };

  const allChainsValid = validationReport && validationReport.every((r) => r.entireChainValid);
  const totalBlocksVerified = validationReport ? validationReport.reduce((acc, r) => acc + r.totalBlocks, 0) : 0;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-white flex items-center gap-3">
            <Link2 className="w-7 h-7 text-cyan-400" />
            Cryptographic Chain of Custody Ledger
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Chronological, append-only Merkle-style hash chained blocks certifying every evidence interaction.
          </p>
        </div>

        <button
          onClick={handleValidateLedger}
          disabled={validating}
          className="px-5 py-2.5 rounded-xl text-xs font-bold text-white bg-gradient-to-r from-cyan-600 to-blue-600 hover:from-cyan-500 hover:to-blue-500 transition-all flex items-center gap-2 shadow-lg shadow-cyan-500/20 disabled:opacity-50"
        >
          <RefreshCw className={`w-4 h-4 ${validating ? 'animate-spin' : ''}`} />
          {validating ? 'Validating Block Chains...' : 'Validate Cryptographic Ledger'}
        </button>
      </div>

      {/* Validation Result Banner */}
      {validationReport && (
        <div className={`p-4 rounded-2xl border flex items-center justify-between gap-4 ${
          allChainsValid ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-300' : 'bg-rose-500/10 border-rose-500/30 text-rose-300'
        }`}>
          <div className="flex items-center gap-3">
            {allChainsValid ? <ShieldCheck className="w-6 h-6 text-emerald-400" /> : <ShieldAlert className="w-6 h-6 text-rose-400" />}
            <div>
              <div className="text-sm font-bold font-mono">
                {allChainsValid ? 'LEDGER INTEGRITY AUDIT PASSED • 100% SOUND' : 'LEDGER AUDIT WARNING • INTEGRITY COMPROMISED'}
              </div>
              <div className="text-xs opacity-90">
                Validated {validationReport.length} evidence chains and {totalBlocksVerified} blocks. Every cryptographic checksum and previous hash pointer verified against raw database rows.
              </div>
            </div>
          </div>
          <div className="text-right text-xs font-mono">
            <span className="font-bold text-base">{totalBlocksVerified}</span>
            <span className="block text-[10px] text-slate-400">Total Blocks Checked</span>
          </div>
        </div>
      )}

      {/* Master Custody Blocks Table */}
      <div className="bg-[#101726] border border-slate-800 rounded-3xl overflow-hidden shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-900/90 text-slate-400 font-mono uppercase tracking-wider border-b border-slate-800">
              <tr>
                <th className="py-3.5 px-4 font-semibold">Block ID</th>
                <th className="py-3.5 px-4 font-semibold">Evidence Reference</th>
                <th className="py-3.5 px-4 font-semibold">Action</th>
                <th className="py-3.5 px-4 font-semibold">Custodian / Target</th>
                <th className="py-3.5 px-4 font-semibold">Block Checksum (SHA-256)</th>
                <th className="py-3.5 px-4 font-semibold">Previous Hash Pointer</th>
                <th className="py-3.5 px-4 font-semibold">Timestamp</th>
                <th className="py-3.5 px-4 font-semibold text-right">Seal</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/80">
              {loading ? (
                <tr>
                  <td colSpan={8} className="py-12 text-center text-slate-500">
                    Loading custody records...
                  </td>
                </tr>
              ) : records.length > 0 ? (
                records.map((r) => (
                  <tr key={r.id} className="hover:bg-slate-800/40 transition-colors">
                    <td className="py-4 px-4 font-mono font-bold text-slate-400">
                      #{r.id}
                    </td>
                    <td className="py-4 px-4 font-mono text-cyan-400 font-bold">
                      {r.evidenceId}
                      <span className="block text-[10px] text-slate-400 font-normal truncate max-w-[150px]">{r.evidenceFileName}</span>
                    </td>
                    <td className="py-4 px-4">
                      <span className={`px-2 py-0.5 rounded text-[10px] font-bold border ${getActionColor(r.action)}`}>
                        {r.action}
                      </span>
                    </td>
                    <td className="py-4 px-4">
                      <div className="font-semibold text-slate-200">{r.performedByFullName || r.performedByUsername}</div>
                      {r.transferredTo && (
                        <div className="text-[10px] text-amber-400 font-mono">To: {r.transferredTo}</div>
                      )}
                    </td>
                    <td className="py-4 px-4 font-mono text-slate-300 text-[11px] select-all max-w-[180px] truncate" title={r.recordHash}>
                      {r.recordHash}
                    </td>
                    <td className="py-4 px-4 font-mono text-slate-400 text-[11px] select-all max-w-[180px] truncate" title={r.previousRecordHash}>
                      {r.previousRecordHash}
                    </td>
                    <td className="py-4 px-4 font-mono text-slate-400 text-[11px]">
                      {new Date(r.timestamp).toLocaleString()}
                    </td>
                    <td className="py-4 px-4 text-right">
                      {r.blockIntegrityValid ? (
                        <span className="inline-flex items-center gap-1 text-[11px] font-semibold text-emerald-400">
                          <ShieldCheck className="w-3.5 h-3.5" /> Sealed
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1 text-[11px] font-semibold text-rose-400">
                          <ShieldAlert className="w-3.5 h-3.5" /> Tampered
                        </span>
                      )}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={8} className="py-12 text-center text-slate-500">
                    No chain of custody blocks registered yet.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
