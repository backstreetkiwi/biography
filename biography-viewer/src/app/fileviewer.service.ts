import { Injectable } from '@angular/core';
import { Output } from '@angular/core';
import { EventEmitter } from '@angular/core';
import { MediaFile } from './types/media-file';

@Injectable({
  providedIn: 'root'
})
export class FileviewerService {

    mediaFile: MediaFile;
    
    @Output() fileSet: EventEmitter<MediaFile> = new EventEmitter();
    
  constructor() { }
  
  show(mediaFile: MediaFile): void {
      this.mediaFile = mediaFile;
      this.fileSet.emit( this.mediaFile );
  }
  
}
