import 'Frontend/generated/jar-resources/flow-component-renderer.js';
import '@vaadin/polymer-legacy-adapter/style-modules.js';
import '@vaadin/combo-box/theme/lumo/vaadin-combo-box.js';
import 'Frontend/generated/jar-resources/comboBoxConnector.js';
import '@vaadin/side-nav/theme/lumo/vaadin-side-nav.js';
import 'Frontend/generated/jar-resources/vaadin-grid-flow-selection-column.js';
import '@vaadin/grid/theme/lumo/vaadin-grid-column.js';
import '@vaadin/accordion/theme/lumo/vaadin-accordion.js';
import '@vaadin/list-box/theme/lumo/vaadin-list-box.js';
import '@vaadin/app-layout/theme/lumo/vaadin-app-layout.js';
import '@vaadin/tooltip/theme/lumo/vaadin-tooltip.js';
import '@vaadin/tabs/theme/lumo/vaadin-tab.js';
import '@vaadin/progress-bar/theme/lumo/vaadin-progress-bar.js';
import '@vaadin/item/theme/lumo/vaadin-item.js';
import '@vaadin/button/theme/lumo/vaadin-button.js';
import 'Frontend/generated/jar-resources/buttonFunctions.js';
import '@vaadin/split-layout/theme/lumo/vaadin-split-layout.js';
import '@vaadin/details/theme/lumo/vaadin-details.js';
import 'Frontend/generated/jar-resources/menubarConnector.js';
import '@vaadin/menu-bar/theme/lumo/vaadin-menu-bar.js';
import 'Frontend/generated/jar-resources/clipboard-helper.js';
import '@vaadin/form-layout/theme/lumo/vaadin-form-layout.js';
import '@vaadin/dialog/theme/lumo/vaadin-dialog.js';
import '@vaadin/vertical-layout/theme/lumo/vaadin-vertical-layout.js';
import '@vaadin/horizontal-layout/theme/lumo/vaadin-horizontal-layout.js';
import 'Frontend/generated/jar-resources/browser-opener.js';
import '@vaadin/grid/theme/lumo/vaadin-grid-column-group.js';
import '@vaadin/accordion/theme/lumo/vaadin-accordion-panel.js';
import '@vaadin/password-field/theme/lumo/vaadin-password-field.js';
import '@vaadin/icon/theme/lumo/vaadin-icon.js';
import '@vaadin/upload/theme/lumo/vaadin-upload.js';
import '@vaadin/side-nav/theme/lumo/vaadin-side-nav-item.js';
import '@vaadin/context-menu/theme/lumo/vaadin-context-menu.js';
import 'Frontend/generated/jar-resources/contextMenuConnector.js';
import 'Frontend/generated/jar-resources/contextMenuTargetConnector.js';
import '@vaadin/form-layout/theme/lumo/vaadin-form-item.js';
import '@vaadin/multi-select-combo-box/theme/lumo/vaadin-multi-select-combo-box.js';
import '@vaadin/grid/theme/lumo/vaadin-grid.js';
import '@vaadin/grid/theme/lumo/vaadin-grid-sorter.js';
import '@vaadin/checkbox/theme/lumo/vaadin-checkbox.js';
import 'Frontend/generated/jar-resources/gridConnector.ts';
import '@vaadin/text-field/theme/lumo/vaadin-text-field.js';
import '@vaadin/icons/vaadin-iconset.js';
import 'Frontend/generated/jar-resources/file-download-wrapper.js';
import '@vaadin/text-area/theme/lumo/vaadin-text-area.js';
import '@vaadin/app-layout/theme/lumo/vaadin-drawer-toggle.js';
import '@vaadin/tabsheet/theme/lumo/vaadin-tabsheet.js';
import '@vaadin/tabs/theme/lumo/vaadin-tabs.js';
import '@vaadin/select/theme/lumo/vaadin-select.js';
import 'Frontend/generated/jar-resources/selectConnector.js';
import '@vaadin/scroller/theme/lumo/vaadin-scroller.js';
import 'Frontend/generated/jar-resources/lit-renderer.ts';
import '@vaadin/notification/theme/lumo/vaadin-notification.js';
import '@vaadin/common-frontend/ConnectionIndicator.js';
import '@vaadin/vaadin-lumo-styles/color-global.js';
import '@vaadin/vaadin-lumo-styles/typography-global.js';
import '@vaadin/vaadin-lumo-styles/sizing.js';
import '@vaadin/vaadin-lumo-styles/spacing.js';
import '@vaadin/vaadin-lumo-styles/style.js';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';

const loadOnDemand = (key) => {
  const pending = [];
  if (key === 'b52f769d73825db8f4fc2f81be2ae704e251ec2ca73f3e621be746825eecd13e') {
    pending.push(import('./chunks/chunk-27bf2a27cd5c07fb33254a63464af56a52b755f297dbcbb2c3ce278cfa5b956c.js'));
  }
  if (key === '499b27238e338a5b53d096658714893e616372a8e02364f09777badc14fe1cc1') {
    pending.push(import('./chunks/chunk-5ab003940a418f77e765bc3aa3ad278d1b34dff8df599b54081610320a0f5f5e.js'));
  }
  if (key === 'ad50e578b9e3670a725d9095b12185ddce3e0017af996cc0a0cc7cadf016f1a5') {
    pending.push(import('./chunks/chunk-e61290c24e78bfec68cf088973451d6165c5d16a2c794740629b3164b493ad99.js'));
  }
  if (key === 'b7664534469d0043a2f201b9b45492a7b2675e228f9e74939edf26048a12e858') {
    pending.push(import('./chunks/chunk-545e0afe237be2afdff5146de344e8d142d1785abb8160f3a1f7696c7d745833.js'));
  }
  if (key === '4b46a2afd87a45f088093888712cea881789d69410a7963fe9129bcef7399364') {
    pending.push(import('./chunks/chunk-00be97d43e8d9f5369a4a58a421b23d198f2fcfc72d1c464f243df97028a75c7.js'));
  }
  if (key === 'ed8503ce8b71c9acd5d0fcf32c26a732693697c4a162c470482736ee4f9fd056') {
    pending.push(import('./chunks/chunk-45456608b463ec707869bbb19ec4def1986804762824c32f2258774faa9cc7ab.js'));
  }
  return Promise.all(pending);
}

window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.loadOnDemand = loadOnDemand;
window.Vaadin.Flow.resetFocus = () => {
 let ae=document.activeElement;
 while(ae&&ae.shadowRoot) ae = ae.shadowRoot.activeElement;
 return !ae || ae.blur() || ae.focus() || true;
}