import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Paper,
  Snackbar,
  Stack,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Tabs,
  Typography,
} from '@mui/material';
import { useEffect, useState } from 'react';
import * as shopsApi from '../../api/shops';
import type { Shop, ShopStatus } from '../../api/shops';
import { extractErrorMessage } from '../../api/client';
import { ReasonDialog } from './ReasonDialog';

const TABS: { label: string; value?: ShopStatus }[] = [
  { label: 'Pending Approval', value: 'PENDING_APPROVAL' },
  { label: 'Active', value: 'ACTIVE' },
  { label: 'Suspended', value: 'SUSPENDED' },
  { label: 'Rejected', value: 'REJECTED' },
  { label: 'Terminated', value: 'TERMINATED' },
  { label: 'All' },
];

const STATUS_COLOR: Record<ShopStatus, 'warning' | 'success' | 'error' | 'default'> = {
  PENDING_APPROVAL: 'warning',
  ACTIVE: 'success',
  SUSPENDED: 'error',
  REJECTED: 'default',
  TERMINATED: 'default',
};

type PendingAction = { type: 'reject' | 'suspend'; shopId: number } | null;

export function ShopsPage() {
  const [tabIndex, setTabIndex] = useState(0);
  const [shops, setShops] = useState<Shop[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [pendingAction, setPendingAction] = useState<PendingAction>(null);
  const [actionSubmitting, setActionSubmitting] = useState(false);
  const [toast, setToast] = useState<string | null>(null);

  const statusFilter = TABS[tabIndex].value;

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const data = await shopsApi.listShops(statusFilter);
      setShops(data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tabIndex]);

  async function handleApprove(shop: Shop) {
    try {
      await shopsApi.approveShop(shop.id);
      setToast(`${shop.name} approved`);
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  }

  async function handleReinstate(shop: Shop) {
    try {
      await shopsApi.reinstateShop(shop.id);
      setToast(`${shop.name} reinstated`);
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  }

  async function handleReasonConfirm(reason: string) {
    if (!pendingAction) return;
    setActionSubmitting(true);
    try {
      if (pendingAction.type === 'reject') {
        await shopsApi.rejectShop(pendingAction.shopId, reason);
        setToast('Shop rejected');
      } else {
        await shopsApi.suspendShop(pendingAction.shopId, reason);
        setToast('Shop suspended');
      }
      setPendingAction(null);
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setActionSubmitting(false);
    }
  }

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 2 }}>
        Shops
      </Typography>

      <Tabs value={tabIndex} onChange={(_, value) => setTabIndex(value)} sx={{ mb: 2 }}>
        {TABS.map((tab) => (
          <Tab key={tab.label} label={tab.label} />
        ))}
      </Tabs>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      <Paper>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Shop</TableCell>
                <TableCell>Owner</TableCell>
                <TableCell>Contact</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={5} align="center" sx={{ py: 6 }}>
                    <CircularProgress size={28} />
                  </TableCell>
                </TableRow>
              ) : shops.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} align="center" sx={{ py: 6 }}>
                    No shops in this category.
                  </TableCell>
                </TableRow>
              ) : (
                shops.map((shop) => (
                  <TableRow key={shop.id} hover>
                    <TableCell>
                      <Typography variant="body2" fontWeight={600}>
                        {shop.name}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {shop.address ?? '—'}
                      </Typography>
                    </TableCell>
                    <TableCell>{shop.ownerName}</TableCell>
                    <TableCell>
                      <Typography variant="body2">{shop.email}</Typography>
                      <Typography variant="caption" color="text.secondary">
                        {shop.phone}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Chip size="small" label={shop.status} color={STATUS_COLOR[shop.status]} />
                    </TableCell>
                    <TableCell align="right">
                      <Stack direction="row" spacing={1} justifyContent="flex-end">
                        {shop.status === 'PENDING_APPROVAL' && (
                          <>
                            <Button size="small" variant="contained" onClick={() => handleApprove(shop)}>
                              Approve
                            </Button>
                            <Button
                              size="small"
                              color="error"
                              onClick={() => setPendingAction({ type: 'reject', shopId: shop.id })}
                            >
                              Reject
                            </Button>
                          </>
                        )}
                        {shop.status === 'ACTIVE' && (
                          <Button
                            size="small"
                            color="error"
                            onClick={() => setPendingAction({ type: 'suspend', shopId: shop.id })}
                          >
                            Suspend
                          </Button>
                        )}
                        {shop.status === 'SUSPENDED' && (
                          <Button size="small" variant="contained" onClick={() => handleReinstate(shop)}>
                            Reinstate
                          </Button>
                        )}
                      </Stack>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>

      <ReasonDialog
        open={pendingAction !== null}
        title={pendingAction?.type === 'reject' ? 'Reject shop registration' : 'Suspend shop'}
        confirmLabel={pendingAction?.type === 'reject' ? 'Reject' : 'Suspend'}
        submitting={actionSubmitting}
        onCancel={() => setPendingAction(null)}
        onConfirm={handleReasonConfirm}
      />

      <Snackbar
        open={toast !== null}
        autoHideDuration={3000}
        onClose={() => setToast(null)}
        message={toast}
      />
    </Box>
  );
}
