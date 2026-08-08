import { createContext, ReactNode, useContext, useMemo, useState } from 'react';
import * as authApi from '../api/auth';
import type { AuthResponse, Role } from '../api/auth';

interface AuthUser {
  userId: number;
  fullName: string;
  role: Role;
  tenantId: number | null;
}

interface AuthContextValue {
  user: AuthUser | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const ACCESS_TOKEN_KEY = 'spgg_access_token';
const REFRESH_TOKEN_KEY = 'spgg_refresh_token';
const USER_KEY = 'spgg_user';

function readStoredUser(): AuthUser | null {
  const raw = localStorage.getItem(USER_KEY);
  return raw ? (JSON.parse(raw) as AuthUser) : null;
}

function persistSession(auth: AuthResponse) {
  localStorage.setItem(ACCESS_TOKEN_KEY, auth.accessToken);
  localStorage.setItem(REFRESH_TOKEN_KEY, auth.refreshToken);
  localStorage.setItem(
    USER_KEY,
    JSON.stringify({
      userId: auth.userId,
      fullName: auth.fullName,
      role: auth.role,
      tenantId: auth.tenantId,
    } satisfies AuthUser),
  );
}

function clearSession() {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(() => readStoredUser());

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      async login(email: string, password: string) {
        const auth = await authApi.login(email, password);
        if (auth.role !== 'SUPER_ADMIN') {
          await authApi.logout(auth.refreshToken).catch(() => undefined);
          throw new Error('Only Super Admin accounts can access this portal.');
        }
        persistSession(auth);
        setUser({ userId: auth.userId, fullName: auth.fullName, role: auth.role, tenantId: auth.tenantId });
      },
      async logout() {
        const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);
        if (refreshToken) {
          try {
            await authApi.logout(refreshToken);
          } catch {
            // best-effort — clear local session regardless
          }
        }
        clearSession();
        setUser(null);
      },
    }),
    [user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within an AuthProvider');
  return context;
}
