export function shouldRestoreDynamicRoutes(
  routeCount: number,
  restoring: boolean,
  initialized: boolean,
): boolean {
  return routeCount === 0 && !restoring && !initialized
}
