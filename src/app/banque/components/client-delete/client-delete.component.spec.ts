import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClientDeleteComponent } from './client-delete.component';

describe('ClientDeleteComponent', () => {
  let component: ClientDeleteComponent;
  let fixture: ComponentFixture<ClientDeleteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClientDeleteComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClientDeleteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
