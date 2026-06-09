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

function positionRelativeTo(element, reference) {
    var rect = reference.getBoundingClientRect();
    var w = element.getBoundingClientRect().width;
    var h = element.getBoundingClientRect().height;
    var edges = {top: rect.top, bottom: rect.bottom, left: rect.left, right: rect.right};
    ['top','bottom','left','right'].forEach(function(attr) {
        var edge = edges[element.getAttribute(attr)];
        if (!edge) return;
        if (attr === 'top') element.style.top = edge + 'px';
        if (attr === 'bottom') element.style.top = (edge - h) + 'px';
        if (attr === 'left') element.style.left = edge + 'px';
        if (attr === 'right') element.style.left = (edge - w) + 'px';
    });
}

function updateMoreDialogLinks(row) {
    document.getElementById('more-dialog-beatmap-link').href = '?b=' + row.getAttribute('data-beatmapid');
    document.getElementById('more-dialog-set-link').href = '?s=' + row.getAttribute('data-beatmapsetid');
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