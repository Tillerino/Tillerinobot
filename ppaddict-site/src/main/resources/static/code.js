function toTime(value) {
    var p = value.split(':');
    var i = parseInt(p[0]);
    var s = parseInt(p[1]);
    return isNaN(i + s) ? null : i * 60 + s;
}

function rangeReq(obj) {
    var base = window.__rangeRequest;
    if (obj.copy) {
        base = JSON.parse(JSON.stringify(base));
        deepMerge(base, obj.copy)
    } else {
        deepMerge(base, obj.mod)
    }
    return JSON.stringify(base)
}

htmx.defineExtension('postrangerequest', {
    onEvent: function (name, evt) {
        if (name === "htmx:configRequest") {
            evt.detail.headers['Content-Type'] = "application/json"
        }
    },
    encodeParameters: function(xhr, parameters, elt) {
        return parameters.json
    }
});

function deepMerge(target, source) {
    for (const key in source) {
        if (source[key] != null && typeof source[key] === 'object') {
            target[key] = target[key] || {};
            deepMerge(target[key], source[key]);
        } else {
            target[key] = source[key];
        }
    }
}

function modifyRule(selector, property, value) {
  for (const sheet of document.styleSheets) {
    for (const rule of sheet.cssRules) {
      if (rule.selectorText === selector) {
        rule.style.setProperty(property, value);
        return true;
      }
    }
  }
  return false;
}

function positionRelativeTo(element, reference, gap) {
    gap = gap || 0;
    var rect = reference.getBoundingClientRect();
    var w = element.getBoundingClientRect().width;
    var h = element.getBoundingClientRect().height;
    var edges = {top: rect.top, bottom: rect.bottom, left: rect.left, right: rect.right};
    ['top','bottom','left','right'].forEach(function(attr) {
        var refAttr = element.getAttribute(attr);
        var edge = edges[refAttr];
        if (!edge) return;
        if (attr === 'top') element.style.top = (edge + (refAttr === 'bottom' ? gap : 0)) + 'px';
        if (attr === 'bottom') element.style.top = (edge - h - (refAttr === 'top' ? gap : 0)) + 'px';
        if (attr === 'left') element.style.left = (edge + (refAttr === 'right' ? gap : 0)) + 'px';
        if (attr === 'right') element.style.left = (edge - w) + 'px';
    });
}

function updateMoreDialogLinks(row) {
    document.getElementById('more-dialog-beatmap-link').href = '?b=' + row.getAttribute('data-beatmapid');
    document.getElementById('more-dialog-set-link').href = '?s=' + row.getAttribute('data-beatmapsetid');
}

function updateEditDialog(row) {
    var comment = row.querySelector('.comments');
    var commentText = comment && comment.firstChild ? comment.firstChild.textContent.trim() : '';
    document.getElementById('edit-dialog-beatmapid').value = row.getAttribute('data-beatmapid');
    document.getElementById('edit-dialog-mods').value = row.getAttribute('data-mods');
    document.getElementById('edit-dialog-comment').value = commentText;
}

document.addEventListener('click', function(e) {
    var btn = e.target.closest('[command="show-modal"]');
    if (!btn) return;
    var targetId = btn.getAttribute('commandfor');
    if (!targetId) return;
    var dialog = document.getElementById(targetId);
    dialog.showModal();
    positionRelativeTo(dialog, btn);
});

var _countdownInterval = null;

function startCountdown(seconds, onComplete) {
    if (_countdownInterval) {
        clearInterval(_countdownInterval);
    }
    var resultEl = document.getElementById('settings-save-result');
    var remaining = seconds;
    var dialog = document.getElementById('settings-modal');
    resultEl.textContent = 'Reloading in ' + remaining + 's...';
    _countdownInterval = setInterval(function() {
        if (dialog && !dialog.open) {
            clearInterval(_countdownInterval);
            _countdownInterval = null;
            return;
        }
        remaining--;
        if (remaining <= 0) {
            clearInterval(_countdownInterval);
            _countdownInterval = null;
            onComplete();
        } else {
            resultEl.textContent = 'Reloading in ' + remaining + 's...';
        }
    }, 1000);
}

var WELCOME_VERSION = '6';

function showWelcomeIfNeeded() {
    var match = document.cookie.match(/(?:^|;\s*)welcomeDisplayed=([^;]*)/);
    if (match && match[1] === WELCOME_VERSION) {
        return;
    }
    var dialog = document.getElementById('welcome-dialog');
    if (!dialog) {
        return;
    }
    dialog.show();
    var expires = new Date(Date.now() + 365 * 86400 * 1000).toUTCString();
    document.cookie = 'welcomeDisplayed=' + WELCOME_VERSION + '; expires=' + expires + '; path=/';
}

var _helpFiltersAutoOpened = false;

function closeAllHelp() {
    var stale = document.querySelectorAll('.helppopup, .help-backdrop');
    for (var i = 0; i < stale.length; i++) {
        stale[i].remove();
    }
    if (_helpFiltersAutoOpened) {
        modifyRule('.filter-row', 'display', 'none');
        _helpFiltersAutoOpened = false;
    }
    var welcome = document.getElementById('welcome-dialog');
    if (welcome && welcome.open) {
        welcome.close();
    }
}

function outsideClick(e) {
    if (e && e.target && e.target.closest
            && e.target.closest('.help-trigger, .helppopup, #welcome-dialog')) {
        // clicks on the help triggers and popups and inside the welcome dialog
        // don't count as outside clicks
        return;
    }
    if (e && e.target && e.target.closest) {
        var openDialog = e.target.closest('dialog[open]');
        if (openDialog) {
            if (e.target !== openDialog) {
                // click inside an open dialog (e.g. on the help backdrop): dismiss the help,
                // but don't auto-hide the dialog (like v1's auto-hide partners)
                closeAllHelp();
                return;
            }
            // click on the dialog element itself is a backdrop click: dismiss it
            openDialog.close();
            closeAllHelp();
            return;
        }
    }
    // like v1's auto-hide: clicking anywhere outside a dialog closes it.
    closeAllHelp();
    var settings = document.getElementById('settings-modal');
    if (settings && settings.open) {
        settings.close();
    }
}

var HELP_PLACEMENTS = {
    'below-right': {top: 'bottom', left: 'left'},
    'below-left': {top: 'bottom', right: 'right'},
    'above-right': {bottom: 'top', left: 'left'},
    'right-below': {top: 'top', left: 'right'}
};

function showHelp() {
    closeAllHelp();

    // like v1's HelpElements.showHelp stack: when a dialog with help is open, only its help shows.
    // help anchors inside the dialog render inside it, so they appear above the modal backdrop (top layer).
    var openDialog = document.querySelector('dialog[open]');
    var container = document.body;
    var anchors = [];
    if (openDialog) {
        anchors = collectHelpAnchors(openDialog, true);
        if (anchors.length) {
            container = openDialog;
        }
    }
    if (container === document.body) {
        anchors = collectHelpAnchors(document.body, false);
        var filterRow = document.querySelector('.filter-row');
        if (filterRow && getComputedStyle(filterRow).display === 'none') {
            modifyRule('.filter-row', 'display', 'table-row');
            _helpFiltersAutoOpened = true;
        }
    }

    for (var i = 0; i < anchors.length; i++) {
        var anchor = anchors[i];
        if (anchor.tagName === 'DIALOG' ? !anchor.open : !anchor.offsetParent) {
            continue;
        }
        var popup = document.createElement('div');
        popup.className = 'helppopup';
        var title = anchor.getAttribute('data-help-title');
        popup.innerHTML =
            (title ? '<div class="helppopuptitle">' + title + ':</div>' : '')
            + anchor.getAttribute('data-help-content');
        if (!container.querySelector('.help-backdrop')) {
            // like v1's modal help popups: dim the page behind the help
            var backdrop = document.createElement('div');
            backdrop.className = 'help-backdrop';
            container.appendChild(backdrop);
        }
        container.appendChild(popup);

        var side = anchor.getAttribute('data-help-side') || 'below-right';
        if (side === 'omni') {
            popup.style.top = '0px';
            popup.style.right = '0px';
        } else {
            var placement = HELP_PLACEMENTS[side] || HELP_PLACEMENTS['below-right'];
            for (var attr in placement) {
                popup.setAttribute(attr, placement[attr]);
            }
            positionRelativeTo(popup, anchor, 6);
        }
    }

    if (_helpFiltersAutoOpened && !container.querySelector('.helppopup')) {
        modifyRule('.filter-row', 'display', 'none');
        _helpFiltersAutoOpened = false;
    }
}

// the help content lives on the elements themselves as data-help-* attributes:
// data-help-content (required), data-help-title (optional), data-help-side (optional),
// and data-help-side="omni" pins the popup to the top right corner (like v1's external links).
function collectHelpAnchors(root, includeRoot) {
    var anchors = [];
    if (includeRoot && root.hasAttribute('data-help-content')) {
        anchors.push(root);
    }
    var found = root.querySelectorAll('[data-help-content]');
    for (var i = 0; i < found.length; i++) {
        anchors.push(found[i]);
    }
    return anchors;
}