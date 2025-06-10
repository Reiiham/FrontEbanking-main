import { TestBed } from '@angular/core/testing';

import { QrPaymentService } from './qr-payment.service';

describe('QrPaymentService', () => {
  let service: QrPaymentService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(QrPaymentService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
