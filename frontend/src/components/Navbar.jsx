import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Shield, LogOut, User, Award, ShieldCheck } from 'lucide-react';

export const Navbar = () => {
  const { user, logout } = useAuth();

  const getRoleBadge = (role) => {
    switch (role) {
      case 'ROLE_ADMIN':
        return <span className="px-2 py-0.5 text-xs font-semibold rounded bg-purple-500/20 text-purple-400 border border-purple-500/30">ADMINISTRATOR</span>;
      case 'ROLE_INVESTIGATOR':
        return <span className="px-2 py-0.5 text-xs font-semibold rounded bg-cyan-500/20 text-cyan-400 border border-cyan-500/30">LEAD INVESTIGATOR</span>;
      case 'ROLE_AUDITOR':
        return <span className="px-2 py-0.5 text-xs font-semibold rounded bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">JUDICIAL AUDITOR</span>;
      default:
        return null;
    }
  };

  return (
    <header className="h-16 border-b border-slate-800 bg-[#0e1424]/90 backdrop-blur sticky top-0 z-40 px-6 flex items-center justify-between">
      <div className="flex items-center gap-3">
        <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-cyan-600 to-blue-600 flex items-center justify-center shadow-lg shadow-cyan-500/20">
          <Shield className="w-6 h-6 text-white" />
        </div>
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-lg font-bold text-white tracking-wide">DECMS FORENSICS</h1>
            <span className="text-[10px] px-1.5 py-0.5 rounded font-mono font-semibold bg-cyan-950 text-cyan-400 border border-cyan-800">SHA-256 VAULT</span>
          </div>
          <p className="text-xs text-slate-400">Digital Evidence & Case Management System</p>
        </div>
      </div>

      <div className="flex items-center gap-4">
        {user && (
          <div className="flex items-center gap-3 bg-slate-900/80 border border-slate-800 py-1.5 px-3 rounded-xl">
            <div className="w-8 h-8 rounded-lg bg-slate-800 border border-slate-700 flex items-center justify-center text-cyan-400">
              <User className="w-4 h-4" />
            </div>
            <div className="text-right">
              <div className="flex items-center gap-2">
                <span className="text-xs font-bold text-slate-200">{user.fullName}</span>
                {getRoleBadge(user.role)}
              </div>
              <div className="text-[11px] text-slate-400 flex items-center justify-end gap-1 font-mono">
                <Award className="w-3 h-3 text-cyan-400" />
                <span>{user.badgeNumber || 'STAFF'}</span>
              </div>
            </div>
          </div>
        )}

        <button
          onClick={logout}
          className="p-2 rounded-xl bg-slate-900 hover:bg-rose-500/10 text-slate-400 hover:text-rose-400 border border-slate-800 hover:border-rose-500/30 transition-colors"
          title="Sign out of forensic session"
        >
          <LogOut className="w-4 h-4" />
        </button>
      </div>
    </header>
  );
};
