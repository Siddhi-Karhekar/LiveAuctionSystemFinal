/* ============================================================
   Live Auction System — Main JavaScript
   Handles: countdown timer, AJAX bid-status polling, UI helpers
   ============================================================ */

// ── Countdown Timer ───────────────────────────────────────────────────────────
/**
 * Starts a live countdown inside element #countdown.
 * @param {string} endTimeStr - ISO datetime string from the server
 */
function startCountdown(endTimeStr) {
    var endTime = new Date(endTimeStr).getTime();
    var el = document.getElementById('countdown');
    if (!el) return;

    function tick() {
        var now  = new Date().getTime();
        var diff = endTime - now;

        if (diff <= 0) {
            el.textContent = 'Auction Ended';
            el.style.color = '#a0a0b0';
            // Reload page to update status
            setTimeout(function() { location.reload(); }, 2000);
            return;
        }

        var hours   = Math.floor(diff / (1000 * 60 * 60));
        var minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
        var seconds = Math.floor((diff % (1000 * 60)) / 1000);

        el.textContent =
            pad(hours) + 'h ' + pad(minutes) + 'm ' + pad(seconds) + 's';
    }

    tick();
    setInterval(tick, 1000);
}

function pad(n) { return n < 10 ? '0' + n : n; }


// ── AJAX Bid Status Polling ───────────────────────────────────────────────────
/**
 * Polls /bid?action=status&id=AUCTION_ID every 5 seconds
 * and updates the highest-bid display without a full page reload.
 * @param {number} auctionId
 * @param {string} contextPath - servlet context path (e.g. '/LiveAuctionSystem')
 */
function startBidPolling(auctionId, contextPath) {
    var highBidEl  = document.getElementById('currentHighBid');
    var totalBidEl = document.getElementById('totalBids');
    var statusEl   = document.getElementById('auctionStatusBadge');

    function poll() {
        var xhr = new XMLHttpRequest();
        xhr.open('GET', contextPath + '/bid?action=status&id=' + auctionId, true);
        xhr.onreadystatechange = function() {
            if (xhr.readyState !== 4 || xhr.status !== 200) return;
            try {
                var data = JSON.parse(xhr.responseText);
                if (data.error) return;

                // Update highest bid
                if (highBidEl) {
                    if (data.highestBid !== null && data.highestBid !== undefined) {
                        highBidEl.textContent = '₹' + formatMoney(data.highestBid);
                    }
                }
                // Update total bids count
                if (totalBidEl) {
                    totalBidEl.textContent = data.totalBids + ' bid' + (data.totalBids !== 1 ? 's' : '');
                }
                // Update status badge
                if (statusEl && data.status) {
                    statusEl.className = 'badge badge-' + data.status;
                    statusEl.textContent = capitalize(data.status);
                }
            } catch (e) { /* JSON parse error — skip */ }
        };
        xhr.send();
    }

    poll(); // immediate first call
    setInterval(poll, 5000); // then every 5 s
}


// ── Utility Functions ─────────────────────────────────────────────────────────

/** Format a number as money with 2 decimal places and comma separators. */
function formatMoney(n) {
    return parseFloat(n).toLocaleString('en-IN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

function capitalize(s) {
    return s.charAt(0).toUpperCase() + s.slice(1);
}

/** Auto-dismiss alert messages after 5 seconds. */
document.addEventListener('DOMContentLoaded', function() {
    var alerts = document.querySelectorAll('.alert');
    alerts.forEach(function(alert) {
        setTimeout(function() {
            alert.style.transition = 'opacity 0.5s';
            alert.style.opacity = '0';
            setTimeout(function() { alert.remove(); }, 500);
        }, 5000);
    });
});

/** Confirm dialog for destructive actions. */
function confirmAction(msg) {
    return confirm(msg || 'Are you sure?');
}

/** Preview image before upload. */
function previewImage(inputEl, imgEl) {
    var file = inputEl.files[0];
    if (!file) return;
    var reader = new FileReader();
    reader.onload = function(e) {
        imgEl.src = e.target.result;
        imgEl.style.display = 'block';
    };
    reader.readAsDataURL(file);
}

/** Validate bid form before submit. */
function validateBidForm(minBid) {
    var input = document.getElementById('bidAmountInput');
    if (!input) return true;
    var val = parseFloat(input.value);
    if (isNaN(val) || val < minBid) {
        alert('Your bid must be at least ₹' + formatMoney(minBid));
        input.focus();
        return false;
    }
    return true;
}
