#!/usr/bin/env python3
import argparse
import json
import subprocess
import time

def parse_args():
    parser = argparse.ArgumentParser(description="Batch create 867 DOMA GitHub Issues")
    parser.add_argument("--repo", default="YeamimHossainSajid/uber-doma-architecture", help="GitHub repo")
    parser.add_argument("--dry-run", action="store_true", help="Print without creating")
    parser.add_argument("--limit", type=int, default=867, help="Number of issues to create")
    return parser.parse_args()

def main():
    args = parse_args()
    print(f"🚀 Uber DOMA GitHub Issue Generator: Target {args.repo} (Limit: {args.limit})")
    
    # Read issues from catalog
    with open("docs/ROADMAP_867_ISSUES.md", "r") as f:
        lines = f.readlines()
    
    issues = []
    current_title = ""
    current_body = ""
    current_labels = ""

    for line in lines:
        if line.startswith("- **Issue #"):
            if current_title:
                issues.append((current_title, current_body, current_labels))
            parts = line.split(":** ")
            if len(parts) == 2:
                current_title = parts[1].strip()
            current_body = ""
            current_labels = "doma-task"
        elif "*Category:*" in line:
            if "`" in line:
                current_labels = line.split("`")[1]
        elif "*Description:*" in line:
            current_body = line.split("*Description:*")[1].strip()

    if current_title:
        issues.append((current_title, current_body, current_labels))

    print(f"Loaded {len(issues)} issues from master catalog.")

    count = 0
    for title, body, labels in issues[:args.limit]:
        count += 1
        clean_title = title.replace("`", "")
        if args.dry_run:
            print(f"[{count:03d}/867] [DRY RUN] {clean_title} ({labels})")
        else:
            print(f"[{count:03d}/867] Creating: {clean_title}")
            cmd = [
                "gh", "issue", "create",
                "--repo", args.repo,
                "--title", clean_title,
                "--body", f"{body}\n\n---\n*Part of the Uber DOMA 867-Issue Enterprise Master Specification.*",
                "--label", labels
            ]
            try:
                subprocess.run(cmd, check=True)
                time.sleep(0.5) # rate limit pacing
            except Exception as e:
                print(f"Failed to create #{count}: {e}")

if __name__ == "__main__":
    main()
