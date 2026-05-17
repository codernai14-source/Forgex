/**
 * Fallback page translations - English
 */
export default {
  actions: {
    home: 'Back Home',
    back: 'Go Back',
    retry: 'Reconnect',
  },
  panel: {
    title: 'Status Check',
  },
  '403': {
    eyebrow: 'Permission check failed',
    title: 'Access denied',
    description: 'This account does not have permission to access the feature. Check role grants, tenant scope, or ask an administrator to enable the menu and action permissions.',
    status: 'Access route blocked',
    checks: {
      permission: 'Check whether menu and action permissions are granted',
      role: 'Confirm the current role includes this feature',
      tenant: 'Confirm the current tenant can access this module',
    },
  },
  '404': {
    eyebrow: 'Route not matched',
    title: 'Page not found',
    description: 'The page you requested was not found. The link may have changed, the menu may not be published, or the module route may not be configured.',
    status: 'No valid page found',
    checks: {
      route: 'Check whether the address is correct',
      menu: 'Confirm the menu is published and enabled',
      link: 'Re-enter the target page from Home or the module menu',
    },
  },
  offline: {
    eyebrow: 'Network connection issue',
    title: 'Service connection unavailable',
    description: 'The client cannot connect to Forgex services right now. Check your network, gateway, or VPN status, then reconnect after service is restored.',
    status: 'Waiting for connection',
    checks: {
      network: 'Check local network and intranet connectivity',
      gateway: 'Confirm the API gateway or backend service is available',
      retry: 'Click reconnect after the connection is restored',
    },
  },
}
