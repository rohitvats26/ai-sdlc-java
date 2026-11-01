import { expect, type Locator, type Page } from '@playwright/test';

export class ApprovalsPage {
  constructor(private readonly page: Page) {}

  async open(): Promise<void> {
    await this.page.getByRole('tab', { name: 'Approvals' }).click();
    await expect(this.page.getByRole('heading', { name: 'Pending approvals' })).toBeVisible();
  }

  pendingInvoice(customerName: string): Locator {
    return this.page.getByRole('listitem').filter({ hasText: customerName });
  }

  async approve(customerName: string): Promise<void> {
    const item = this.pendingInvoice(customerName);
    await expect(item).toBeVisible();
    await item.getByRole('button', { name: /^Approve invoice / }).click();
  }

  async expectRefusal(message: RegExp): Promise<void> {
    await expect(this.page.getByRole('alert')).toContainText(message);
  }
}
