/**
 * Profile page strings
 *
 * @author Forgex
 * @version 1.0.0
 */
export default {
  title: 'Profile',
  tabs: {
    basic: 'Basic Info',
    org: 'Organization',
    security: 'Security',
    personalHomepage: 'Personal Homepage',
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
    status: '状态',
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
