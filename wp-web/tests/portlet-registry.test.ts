import { describe, expect, it } from 'vitest';

import { isDisabledPortlet, portletRegistry } from '@/portlets/registry';

describe('portletRegistry', () => {
  it('has unique ids', () => {
    const ids = portletRegistry.map((portlet) => portlet.id);

    expect(new Set(ids).size).toBe(ids.length);
  });

  it('treats OBO portlets as disabled views', () => {
    expect(isDisabledPortlet('portlets.obo.TermRelationships')).toBe(true);
    expect(isDisabledPortlet('portlets.ClassHierarchy')).toBe(false);
  });
});
