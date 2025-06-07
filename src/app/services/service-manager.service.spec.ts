import { TestBed } from '@angular/core/testing';

import { ServiceManagerService } from './service-manager.service';

describe('ServiceManagerService', () => {
  let service: ServiceManagerService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ServiceManagerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
