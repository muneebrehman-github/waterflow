import WaterDropIcon from '@mui/icons-material/WaterDrop';
import { Alert, Box, Button, CircularProgress, Paper, TextField, Typography } from '@mui/material';
import { FormEvent, useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../../auth/AuthContext';
import { extractErrorMessage } from '../../api/client';

export function LoginPage() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  if (user) {
    return <Navigate to="/shops" replace />;
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await login(email, password);
      navigate('/shops', { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        bgcolor: 'grey.100',
      }}
    >
      <Paper elevation={3} sx={{ p: 4, width: 380 }}>
        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', mb: 3 }}>
          <WaterDropIcon color="primary" sx={{ fontSize: 48 }} />
          <Typography variant="h5" sx={{ mt: 1 }}>
            SaafPaniGharGhar
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Super Admin Portal
          </Typography>
        </Box>
        <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <TextField
            label="Email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            fullWidth
          />
          <TextField
            label="Password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            fullWidth
          />
          {error && <Alert severity="error">{error}</Alert>}
          <Button type="submit" variant="contained" disabled={loading} size="large">
            {loading ? <CircularProgress size={24} color="inherit" /> : 'Log In'}
          </Button>
        </Box>
      </Paper>
    </Box>
  );
}
