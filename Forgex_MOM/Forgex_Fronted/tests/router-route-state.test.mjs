import assert from 'node:assert/strict'
import { shouldRestoreDynamicRoutes } from '../src/router/routeState.mts'

assert.equal(shouldRestoreDynamicRoutes(0, false, false), true)
assert.equal(
  shouldRestoreDynamicRoutes(0, false, true),
  false,
  'an initialized tenant with no dynamic menus must not restore routes again',
)
console.log('router route state test passed')
