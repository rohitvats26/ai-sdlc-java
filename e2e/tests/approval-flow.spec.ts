import { expect, test } from '@playwright/test';
import { ApprovalsPage } from '../pages/approvals.page';
import { InvoicesPage } from '../pages/invoices.page';
import { LoginPage } from '../pages/login.page';

function uniqueCustomer(label: string): string {
  return `E2E ${label} ${Date.now()}-${Math.random().toString(36).slice(2)}`;
}

test('Manager approval is refused above the Manager limit', async ({ page }) => {
  const customer = uniqueCustomer('manager refusal');
  const login = new LoginPage(page);
  const invoices = new InvoicesPage(page);
  const approvals = new ApprovalsPage(page);

  await page.goto('/');
  await login.signIn('demo.manager');
  const row = await invoices.createAndSubmit(customer, '50000.01');
  await approvals.open();
  await approvals.approve(customer);

  await approvals.expectRefusal(/50,000 Manager limit.*Director is required/);
  await expect(row.getByRole('cell', { name: 'Pending Approval' })).toBeVisible();
  await expect(row.getByRole('cell', { name: '$50,000.01' })).toBeVisible();
  await expect(row.getByRole('cell', { name: '0 of 1' })).toBeVisible();
});

test('Director can approve an invoice above the Manager limit', async ({ page }) => {
  const customer = uniqueCustomer('director control');
  const login = new LoginPage(page);
  const invoices = new InvoicesPage(page);
  const approvals = new ApprovalsPage(page);

  await page.goto('/');
  await login.signIn('demo.director');
  const row = await invoices.createAndSubmit(customer, '50000.01');
  await approvals.open();
  await approvals.approve(customer);

  await expect(row.getByRole('cell', { name: 'Approved' })).toBeVisible();
  await expect(row.getByRole('cell', { name: '$50,000.01' })).toBeVisible();
  await expect(row.getByRole('cell', { name: '1 of 1' })).toBeVisible();
});

test('Two distinct Directors approve a high-value invoice; duplicate approval is refused', async ({ page }) => {
  const customer = uniqueCustomer('two directors');
  const login = new LoginPage(page);
  const invoices = new InvoicesPage(page);
  const approvals = new ApprovalsPage(page);

  await page.goto('/');
  await login.signIn('demo.director');
  const row = await invoices.createAndSubmit(customer, '600000');
  await approvals.open();
  await approvals.approve(customer);

  await expect(row.getByRole('cell', { name: 'Pending Approval' })).toBeVisible();
  await expect(row.getByRole('cell', { name: '1 of 2' })).toBeVisible();
  await expect(approvals.pendingInvoice(customer)).toContainText('Approved so far: demo.director');

  await approvals.approve(customer);
  await approvals.expectRefusal(/this user has already approved it/i);
  await expect(row.getByRole('cell', { name: '1 of 2' })).toBeVisible();

  await login.signOut();
  await login.signIn('demo.director2');
  await approvals.open();
  await approvals.approve(customer);

  await expect(row.getByRole('cell', { name: 'Approved' })).toBeVisible();
  await expect(row.getByRole('cell', { name: '2 of 2' })).toBeVisible();
});
