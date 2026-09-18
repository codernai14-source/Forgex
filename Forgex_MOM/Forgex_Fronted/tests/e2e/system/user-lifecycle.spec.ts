import { expect, test } from '@playwright/test'

const baseUrl = process.env.E2E_BASE_URL
const username = process.env.E2E_USERNAME
const password = process.env.E2E_PASSWORD
const userPath = process.env.E2E_USER_PATH || '/system/user'

test.describe('system user lifecycle', () => {
  test.skip(!baseUrl || !username || !password, 'Set E2E_BASE_URL, E2E_USERNAME, and E2E_PASSWORD to run against a local Forgex environment.')

  test('logs in, creates a user, then removes that user', async ({ page }) => {
    const generatedAccount = `e2e${Date.now().toString().slice(-10)}`
    const generatedName = `E2E ${generatedAccount}`

    await page.goto('/login')
    await page.getByTestId('login-account').fill(username!)
    await page.getByTestId('login-password').fill(password!)

    const captcha = page.getByTestId('login-captcha')
    if (await captcha.isVisible()) {
      const captchaValue = process.env.E2E_CAPTCHA
      if (!captchaValue) {
        throw new Error('The local login page requires an image captcha. Set E2E_CAPTCHA or configure a test environment without it.')
      }
      await captcha.fill(captchaValue)
    }

    await page.getByTestId('login-submit').click()

    const tenantOverlay = page.locator('.identity-overlay')
    if (await tenantOverlay.isVisible()) {
      const requestedTenant = process.env.E2E_TENANT_NAME
      const tenant = requestedTenant
        ? tenantOverlay.locator('.tenant-card').filter({ hasText: requestedTenant })
        : tenantOverlay.locator('.tenant-card').first()
      await expect(tenant).toBeVisible()
      await tenant.click()
      await page.getByTestId('tenant-confirm').click()
    }

    await expect(page).not.toHaveURL(/\/login$/)
    await page.goto(userPath)
    await expect(page.getByTestId('user-add')).toBeVisible()

    await page.getByTestId('user-add').click()
    await page.getByTestId('user-form-account').fill(generatedAccount)
    await page.getByTestId('user-form-name').fill(generatedName)
    await page.getByTestId('user-form-submit').click()
    await expect(page.getByText(generatedName)).toBeVisible()

    const row = page.getByRole('row').filter({ hasText: generatedName })
    await expect(row).toBeVisible()
    await row.locator('.fx-action-group__more').click()
    await page.locator('[data-guide-id="sys-user-row-delete"]').click()
    await page.locator('.ant-modal-confirm .ant-btn-primary').click()
    await expect(page.getByText(generatedName)).toHaveCount(0)
  })
})
