/**
 * Profile page strings
 */
export default {
  title: 'Profile',
  tabs: {
    basic: 'Basic Info',
    org: 'Organization',
    security: 'Security',
    personalHomepage: 'Personal Homepage',
    guide: 'Guide Settings',
  },
  fields: {
    account: 'Account',
    username: 'Username',
    email: 'Email',
    phone: 'Phone',
    gender: 'Gender',
    department: 'Department',
    position: 'Position',
    entryDate: 'Entry Date',
    status: 'Status',
    oldPassword: 'Current Password',
    newPassword: 'New Password',
    confirmPassword: 'Confirm Password',
  },
  gender: {
    male: 'Male',
    female: 'Female',
    unknown: 'Unknown',
  },
  actions: {
    save: 'Save',
    reset: 'Reset',
    changePassword: 'Change Password',
  },
  personalHomepage: {
    title: 'My Personal Homepage',
    description:
      'Your homepage layout is saved here. Restoring defaults will inherit tenant or public configuration again.',
  },
  guide: {
    title: 'Guide and Baby Mode',
    description:
      'Control whether the system guide should appear automatically, and replay the system guide when needed.',
    babyModeTitle: 'Enable Baby Mode',
    babyModeDesc: 'When enabled, guides that were completed or skipped can be shown again proactively.',
    systemPageGuideTitle: 'System Page Guides',
    systemPageGuideDesc:
      'When disabled, later system management pages will no longer auto-open guides. The current page is not affected.',
    replayTitle: 'Replay System Guide',
    replayDesc:
      'Clear the guide disable state and reset the system homepage and system page guides to pending.',
    replayAction: 'Replay',
  },
  validation: {
    oldPasswordRequired: 'Please enter current password',
    newPasswordRequired: 'Please enter new password',
    newPasswordMin: 'Password must be at least 6 characters',
    confirmPasswordRequired: 'Please confirm new password',
    passwordMismatch: 'Passwords do not match',
  },
  message: {
    loadFailed: 'Failed to load user profile',
    passwordChanged: 'Password changed. Please sign in again.',
    passwordChangeFailed: 'Failed to change password',
  },
}
