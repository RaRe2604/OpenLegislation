<!-- Detail Template -->
<script type="text/ng-template" id="mismatchDetailWindow">
  <md-dialog aria-label="Mismatch Detail" class="detail-diff-dialog">
    <span ng-click="cancel()" class="icon-cross mismatch-diff-view-exit"></span>
    <md-content>
      <md-content class="mismatch-diff-view-top-half">
        <div layout="row" layout-align="space-between center">

          <md-card class="mismatch-diff-info-card">
            <p>Last Reported: {{currentMismatch.observedDate}}</p>

            <!-- Bill Id Fields -->
            <p ng-if="contentType == 'BILL'">
              {{reportType | contentType}} Number:
              <a class="white-2-blue inactive-link" target="_blank">
                {{currentMismatch.bill}}
              </a>
            </p>
            <p ng-if="contentType == 'BILL'">Session Year: {{currentMismatch.session.year}}</p>

            <!-- Calendar Id Fields -->
            <p ng-if="contentType == 'CALENDAR'">Calendar Number: {{currentMismatch.calNo}}</p>
            <p ng-if="contentType == 'CALENDAR'">Session Date: {{currentMismatch.calDate}}</p>

            <!-- Agenda Id Fields -->
            <p ng-if="contentType == 'AGENDA'">Year: {{currentMismatch.key.agendaId.year}}</p>
            <p ng-if="contentType == 'AGENDA'">Agenda: {{currentMismatch.agendaNo}}</p>
            <p ng-if="contentType == 'AGENDA'">Committee: {{currentMismatch.key.committeeId.name}}</p>

            <p>Error Type: {{currentMismatch.mismatchType | mismatchType:currentMismatch.datasource}}</p>
          </md-card>

          <!--------------------------------- ADDED SECTION  --------------------------------->

          <div style="border-radius: 25px; text-align: center; box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);">
          <p style="font-weight: bold;"> Please Choose One: </p>
          <md-button style="border-radius: 15px;" class="md-primary md-raised" ng-click="showGen()">
          Generate Issue
        </md-button>
          <md-button style="border-radius: 15px;" class="md-primary md-raised" ng-click="showLink()">
            Link Issue
          </md-button>
            <br>
            <br>
          </div>

          <!---------------------------------------------------------------------------------->

          <md-card class="mismatch-diff-text-controls">
            <select ng-model="textControls.whitespace" ng-change="formatDisplayData()"
                    ng-options="value as label for (value, label) in whitespaceOptions"></select>
            <md-checkbox ng-model="textControls.removeLinePageNums" ng-change="formatDisplayData()">
              Strip Line/Page Numbers
            </md-checkbox>
            <md-checkbox ng-model="textControls.capitalize" ng-change="formatDisplayData()">All Caps</md-checkbox>
          </md-card>
        </div>
      </md-content>

      <md-content>
        <div class="mismatch-diff-side">
           <div class="mismatch-diff-source-label">
             <a ng-href="{{currentMismatch.key | referenceUrl:currentMismatch.datasource:currentMismatch.contentType}}"˜˜
                target="_blank">
               <span ng-bind="currentMismatch.datasource | dataSourceRef"></span>
             </a>
          </div>

          <div id="mismatch-diff-reference" class="mismatch-diff-container">˜
            <mismatch-diff show-lines="false" left="observedData" right="referenceData"></mismatch-diff>
          </div>
        </div>

        <div class="mismatch-diff-side">
          <div class="mismatch-diff-source-label">
            <a ng-href="{{currentMismatch.key | contentUrl:currentMismatch.datasource:currentMismatch.contentType}}"
               target="_blank">
              <span ng-bind="currentMismatch.datasource | dataSourceData"></span>
            </a>
          </div>

          <div id="mismatch-diff-observed" class="mismatch-diff-container">
            <mismatch-diff show-lines="false" left="observedData" right="referenceData"></mismatch-diff>
          </div>
        </div>
      </md-content>
    </md-content>
  </md-dialog>
</script>