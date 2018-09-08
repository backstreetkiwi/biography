import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ThumbSizeSelectorComponent } from './thumb-size-selector.component';

describe('ThumbSizeSelectorComponent', () => {
  let component: ThumbSizeSelectorComponent;
  let fixture: ComponentFixture<ThumbSizeSelectorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ThumbSizeSelectorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ThumbSizeSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
