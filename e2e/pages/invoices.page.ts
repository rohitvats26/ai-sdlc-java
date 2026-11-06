import { expect, type Locator, type Page } from '@playwright/test';

export class InvoicesPage {
  constructor(private readonly page: Page) {}

  async createAndSubmit(customerName: string, amount: string): Promise<Locator> {
    await this.page.getByLabel('Customer name').fill(customerName);
    await this.page.getByLabel('Amount').fill(amount);
    await this.page.getByRole('button', { name: 'Create invoice' }).click();

    const row = this.invoiceRow(customerName);
    await expect(row).toBeVisible();
    const invoiceId = await row.getByRole('cell').first().innerText();
    await row.getByRole('button', { name: `Submit invoice ${invoiceId} for approval` }).click();
    await expect(row.getByRole('cell', { name: 'Pending Approval' })).toBeVisible();
    return row;
  }

  invoiceRow(customerName: string): Locator {
    return this.page.getByRole('row').filter({ hasText: customerName });
  }
}
