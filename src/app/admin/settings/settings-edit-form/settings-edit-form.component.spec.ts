import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SettingsEditFormComponent } from './settings-edit-form.component';

describe('SettingsEditFormComponent', () => {
  let component: SettingsEditFormComponent;
  let fixture: ComponentFixture<SettingsEditFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SettingsEditFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SettingsEditFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
