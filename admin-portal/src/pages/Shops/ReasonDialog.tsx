import { Button, Dialog, DialogActions, DialogContent, DialogTitle, TextField } from '@mui/material';
import { useState } from 'react';

interface ReasonDialogProps {
  open: boolean;
  title: string;
  confirmLabel: string;
  onCancel: () => void;
  onConfirm: (reason: string) => void;
  submitting: boolean;
}

export function ReasonDialog({ open, title, confirmLabel, onCancel, onConfirm, submitting }: ReasonDialogProps) {
  const [reason, setReason] = useState('');

  function handleClose() {
    setReason('');
    onCancel();
  }

  function handleConfirm() {
    onConfirm(reason);
    setReason('');
  }

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        <TextField
          autoFocus
          margin="dense"
          label="Reason"
          fullWidth
          multiline
          minRows={3}
          value={reason}
          onChange={(e) => setReason(e.target.value)}
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose}>Cancel</Button>
        <Button onClick={handleConfirm} variant="contained" disabled={!reason.trim() || submitting}>
          {confirmLabel}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
