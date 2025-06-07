import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClientStatusToggleComponent } from './client-status-toggle.component';

describe('ClientStatusToggleComponent', () => {
  let component: ClientStatusToggleComponent;
  let fixture: ComponentFixture<ClientStatusToggleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClientStatusToggleComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClientStatusToggleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
