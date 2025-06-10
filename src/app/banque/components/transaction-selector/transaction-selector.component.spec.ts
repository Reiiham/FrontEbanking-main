import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TransactionSelectorComponent } from './transaction-selector.component';

describe('TransactionSelectorComponent', () => {
  let component: TransactionSelectorComponent;
  let fixture: ComponentFixture<TransactionSelectorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TransactionSelectorComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TransactionSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
