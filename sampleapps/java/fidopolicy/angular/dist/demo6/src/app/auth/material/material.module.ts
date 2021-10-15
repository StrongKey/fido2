import { NgModule }      from '@angular/core';
import {MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatDividerModule} from '@angular/material/divider';
import {MatTabsModule} from '@angular/material/tabs';
import {MatIconModule} from '@angular/material/icon';
import {MatListModule} from '@angular/material/list';
import {MatDialogModule} from '@angular/material/dialog';


@NgModule({
  declarations: [],
  imports: [],
  exports: [MatCardModule,
  MatButtonModule,
  MatInputModule,
  MatSelectModule,
  MatDividerModule,
  MatTabsModule,
  MatIconModule,
  MatListModule,
  MatDialogModule]
  })
export class MaterialModule {}
