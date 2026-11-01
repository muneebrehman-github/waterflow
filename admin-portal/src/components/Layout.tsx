import LogoutIcon from '@mui/icons-material/Logout';
import WaterDropIcon from '@mui/icons-material/WaterDrop';
import { AppBar, Box, Container, IconButton, Toolbar, Typography } from '@mui/material';
import { Outlet } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export function Layout() {
  const { user, logout } = useAuth();

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'grey.50' }}>
      <AppBar position="static" color="primary" enableColorOnDark>
        <Toolbar>
          <WaterDropIcon sx={{ mr: 1 }} />
          <Typography variant="h6" sx={{ flexGrow: 1 }}>
            SaafPaniGharGhar — Super Admin
          </Typography>
          {user && (
            <>
              <Typography variant="body2" sx={{ mr: 2 }}>
                {user.fullName}
              </Typography>
              <IconButton color="inherit" onClick={() => logout()} title="Log out">
                <LogoutIcon />
              </IconButton>
            </>
          )}
        </Toolbar>
      </AppBar>
      <Container sx={{ py: 4 }}>
        <Box
          sx={{
            mb: 3,
            p: 2,
            borderRadius: 2,
            bgcolor: 'info.light',
            color: 'info.contrastText',
          }}
        >
          <Typography variant="subtitle1" fontWeight={700}>
            Also available in FTS Control Center
          </Typography>
          <Typography variant="body2">
            This Super Admin portal remains available. Control Center (port 8010) is the unified ops console.
          </Typography>
        </Box>
        <Outlet />
      </Container>
    </Box>
  );
}
