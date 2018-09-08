import { TestBed, inject } from '@angular/core/testing';

import { FileviewerService } from './fileviewer.service';

describe('FileviewerService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [FileviewerService]
    });
  });

  it('should be created', inject([FileviewerService], (service: FileviewerService) => {
    expect(service).toBeTruthy();
  }));
});
