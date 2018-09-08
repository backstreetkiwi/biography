import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ZoomViewerComponent } from './zoom-viewer.component';

describe('ZoomViewerComponent', () => {
  let component: ZoomViewerComponent;
  let fixture: ComponentFixture<ZoomViewerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ZoomViewerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ZoomViewerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
